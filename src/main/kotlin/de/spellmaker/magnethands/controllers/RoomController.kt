package de.spellmaker.magnethands.controllers

import de.spellmaker.magnethands.entities.RoomEntity
import de.spellmaker.magnethands.entities.RoundEntity
import de.spellmaker.magnethands.entities.SnippetEntity
import de.spellmaker.magnethands.entities.UserEntity
import de.spellmaker.magnethands.repositories.RoomRepository
import de.spellmaker.magnethands.repositories.RoundRepository
import de.spellmaker.magnethands.repositories.UserRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post

/**
 * Controller for room entities
 */
@Controller("/api/room")
class RoomController(
  private val users: UserRepository,
  private val rooms: RoomRepository,
  private val rounds: RoundRepository
) {

  /**
   * Get the details of the current room
   */
  @Get("/{room}")
  fun getRoomDetails(
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return users.withUser(magnetfriends_id, magnetfriends_secret) { user ->
      rooms.withRoom(room) { currentRoom ->
        if (currentRoom.members.contains(user)) {
          HttpResponse.ok(RoomDetails(
            id = currentRoom.id!!,
            name = currentRoom.name,
            owner = currentRoom.owner,
            currentRound = currentRoom.rounds.size,
            members = currentRoom.members,
            totalSnippets = currentRoom.rounds.lastOrNull()?.snippets?.size ?: -1,
            shownSnippets = (currentRoom.rounds.lastOrNull()?.snippets?.filter { it.isShown } ?: emptyList()).map {
              SnippetDTO(
                used = it.isUsed,
                content = it.content,
                id = it.id!!
              )
            }
          ))
        } else {
          HttpResponse.badRequest("not a member of the indicated room")
        }
      }
    }
  }

  /**
   * Start a new round in the current room
   */
  @Post("/{room}/round")
  fun startRound(
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return users.withUser(magnetfriends_id, magnetfriends_secret) { user ->
      rooms.withRoom(room) { currentRoom ->
        if (currentRoom.owner.id == user.id) {
          val round = RoundEntity(
            id = null,
            room = currentRoom,
            snippets = emptyList()
          )
          rounds.save(round)
          HttpResponse.ok("saved")
        } else {
          HttpResponse.badRequest("only the room owner may start a new round")
        }
      }
    }
  }

  /**
   * Join a room
   */
  @Post("/{room}")
  fun joinRoom(
    room: Int,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return users.withUser(magnetfriends_id, magnetfriends_secret) { user ->
      rooms.withRoom(room) { currentRoom ->
        val newRoom = RoomEntity(
          id = currentRoom.id,
          name = currentRoom.name,
          rounds = currentRoom.rounds,
          owner = currentRoom.owner,
          members = currentRoom.members.plus(user)
        )
        rooms.update(newRoom)
        HttpResponse.ok("joined room")
      }
    }
  }

  /**
   * Create a new room
   */
  @Post
  @Consumes("text/plain")
  fun createRoom(
    @Body name: String,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    return users.withUser(magnetfriends_id, magnetfriends_secret) { user ->
      val room = RoomEntity(
        id = null,
        name = name,
        rounds = emptyList(),
        owner = user,
        members = setOf(user)
      )
      HttpResponse.ok(rooms.save(room).id)
    }
  }
}

/**
 * Perform an action ensuring a room exists
 */
fun RoomRepository.withRoom(id: Int, action: (RoomEntity) -> HttpResponse<Any>): HttpResponse<Any> {
  val currentRoom = this.findById(id)
  return if (currentRoom.isPresent) {
    action(currentRoom.get())
  } else {
    HttpResponse.notFound("no such room")
  }
}

/**
 * Details of a room shown to an api user
 *
 * @param id the room id
 * @param name the room name
 * @param owner the owner of the room
 * @param currentRound The number of the active round
 * @param members The members of the room
 * @param totalSnippets snippets submitted in total
 * @param shownSnippets snippets currently shown to all users
 */
data class RoomDetails(
  val id: Int,
  val name: String,
  val owner: UserEntity,
  val currentRound: Int,
  val members: Set<UserEntity>,
  val totalSnippets: Int,
  val shownSnippets: List<SnippetDTO>
)