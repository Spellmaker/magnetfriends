package de.spellmaker.magnethands.controllers

import de.spellmaker.magnethands.entities.RoundEntity
import de.spellmaker.magnethands.entities.SnippetEntity
import de.spellmaker.magnethands.entities.UserEntity
import de.spellmaker.magnethands.repositories.RoomRepository
import de.spellmaker.magnethands.repositories.SnippetRepository
import de.spellmaker.magnethands.repositories.UserRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import javax.transaction.Transactional

/**
 * Controller for snippet entities
 */
@Controller("/api/room/{room}/snippet")
open class SnippetController(
  private val users: UserRepository,
  private val rooms: RoomRepository,
  private val snippets: SnippetRepository
) {

  private fun withValidation(
    room: Int,
    userId: String?,
    secret: String?,
    action: (RoundEntity, UserEntity) -> HttpResponse<Any>
  ): HttpResponse<Any> {
    return users.withUser(userId, secret) { userEntity ->
      rooms.withRoom(room) { roomEntity ->
        if (roomEntity.members.contains(userEntity)) {
          if (roomEntity.rounds.isNotEmpty()) {
            action(roomEntity.rounds.last(), userEntity)
          } else {
            HttpResponse.badRequest("no round was started yet")
          }
        } else {
          HttpResponse.badRequest("user must be a member of the room")
        }
      }
    }
  }

  /**
   * Use a snippet from your pool
   */
  @Post("{snippetId}")
  open fun useSnippet(
    room: Int,
    snippetId: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return withValidation(room, magnetfriends_id, magnetfriends_secret) { round, user ->
      val snippet = round.snippets.firstOrNull { snippetId == it.id }
      when {
        snippet == null -> {
          HttpResponse.badRequest("snippet not found")
        }
        snippet.holder?.id != user.id -> {
          HttpResponse.badRequest("this is not held by you")
        }
        snippet.isUsed -> {
          HttpResponse.badRequest("this snippet is already used")
        }
        else -> {
          val updatedSnippet = SnippetEntity(
            id = snippet.id,
            content = snippet.content,
            holder = snippet.holder,
            round = snippet.round,
            creator = snippet.creator,
            isShown = true,
            isUsed = true
          )
          snippets.update(updatedSnippet)
          HttpResponse.ok("snippet used")
        }
      }
    }
  }

  /**
   * Hide all snippets
   */
  @Post("/hide")
  open fun hideAllSnippets(
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return withValidation(room, magnetfriends_id, magnetfriends_secret) { round, _ ->
      val all = round.snippets.map {
        SnippetEntity(
          id = it.id,
          content = it.content,
          holder = it.holder,
          round = it.round,
          creator = it.creator,
          isShown = false,
          isUsed = it.isUsed
        )
      }
      all.forEach { snippets.update(it) }
      HttpResponse.ok("all snippets hidden")
    }
  }

  /**
   * Provide a new snippet to the pool
   */
  @Post
  @Consumes("text/plain")
  open fun supplySnippet(
    room: Int,
    @Body content: String,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return withValidation(room, magnetfriends_id, magnetfriends_secret) { round, user ->
      val newSnippet = SnippetEntity(
        id = null,
        content = content,
        holder = null,
        round = round,
        creator = user,
        isShown = false,
        isUsed = false
      )
      snippets.save(newSnippet)
      HttpResponse.ok("stored a new snippet")
    }
  }

  /**
   * Gets snippet statistics
   */
  @Get
  open fun getSnippetInfo(
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return withValidation(room, magnetfriends_id, magnetfriends_secret) { round, user ->
      HttpResponse.ok(SnippetInfo(
        totalSnippets = round.snippets.size,
        yourSuppliedSnippets = round.snippets.count { it.creator.id == user.id },
        unclaimedSnippets = round.snippets.count { it.holder == null },
        yourHeldSnippets = round.snippets.filter { it.holder?.id == user.id }.map {
          SnippetDTO(
            used = it.isUsed,
            content = it.content,
            id = it.id!!
          )
        }
      ))
    }
  }

  /**
   * Draw a number of snippets from the pool, assigning them to the current user
   */
  @Get("/draw{?count}")
  @Transactional
  open fun drawSnippetsFromPool(
    count: Int?,
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return withValidation(room, magnetfriends_id, magnetfriends_secret) { round, user ->
      if (count == null) {
        HttpResponse.badRequest("must provide number of snippets to draw")
      } else {
        val unused = round.snippets.filter { it.holder == null }.toMutableList()
        if (unused.size < count) {
          HttpResponse.badRequest("not enough elements left")
        } else {
          unused.shuffle()
          val nowUsed = (0 until count)
            .map { index -> unused[index] }
            .map {
              SnippetEntity(
                id = it.id,
                content = it.content,
                holder = user,
                round = round,
                creator = user,
                isShown = false,
                isUsed = false
              )
            }
          nowUsed.forEach { snippets.update(it) }
          HttpResponse.ok(nowUsed.map {
            SnippetDTO(
              used = it.isUsed,
              content = it.content,
              id = it.id!!
            )
          })
        }
      }
    }
  }
}

/**
 * Info about snippets
 *
 * @param totalSnippets total snippets in the round
 * @param yourSuppliedSnippets snippet count supplied by you
 * @param unclaimedSnippets snippets not yet claimed by someone
 * @param yourHeldSnippets snippets held by you
 */
data class SnippetInfo(
  val totalSnippets: Int,
  val yourSuppliedSnippets: Int,
  val unclaimedSnippets: Int,
  val yourHeldSnippets: List<SnippetDTO>
)

/**
 * Snippet info
 *
 * @param used if the snippet has been used
 * @param content the content of the snippet
 * @param id the id
 */
data class SnippetDTO(
  val used: Boolean,
  val content: String,
  val id: Int
)
