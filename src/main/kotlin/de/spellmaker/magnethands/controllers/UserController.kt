package de.spellmaker.magnethands.controllers

import de.spellmaker.magnethands.entities.UserEntity
import de.spellmaker.magnethands.repositories.UserRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CookieValue
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.cookie.Cookie

/**
 * Controller for user entities
 */
@Controller("/api/user")
class UserController(
  private val userRepository: UserRepository
) {

  /**
   * Create a new user
   */
  @Post
  @Consumes("application/json")
  fun createUser(
    @Body payload: UserCreationPayload,
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    val allUsers = userRepository.findAll()
    return if (allUsers.any { it.name == payload.name }) {
      val user = allUsers.firstOrNull { it.name == payload.name && it.secret == payload.secret }
      if (user != null) {
        HttpResponse.ok<Any>(user)
          .cookie(Cookie.of("magnetfriends_id", "${user.id}").path("/"))
          .cookie(Cookie.of("magnetfriends_name", user.name).path("/"))
          .cookie(Cookie.of("magnetfriends_secret", user.secret).path("/"))
      } else {
        HttpResponse.badRequest("Could not authenticate users")
      }
    } else {
      val result = userRepository.save(UserEntity(name = payload.name, secret = payload.secret))
      HttpResponse.ok<Any>(result)
        .cookie(Cookie.of("magnetfriends_id", "${result.id}").path("/"))
        .cookie(Cookie.of("magnetfriends_name", result.name).path("/"))
        .cookie(Cookie.of("magnetfriends_secret", result.secret).path("/"))
    }
  }

  /**
   * Display current user details
   */
  @Get
  fun displayUser(
    @CookieValue magnetfriends_id: String?,
    @CookieValue magnetfriends_secret: String?
  ): HttpResponse<Any> {
    println(magnetfriends_id)
    println(magnetfriends_secret)
    return userRepository.withUser(magnetfriends_id, magnetfriends_secret) {
      HttpResponse.ok(it)
    }
  }
}

/**
 * Payload for user creation
 *
 * @param name user name
 * @param secret The secret for the user
 */
data class UserCreationPayload(val name: String, val secret: String)

/**
 * Perform an action with the user and only with the user
 */
fun UserRepository.withUser(
  id: String?,
  secret: String?,
  action: (UserEntity) -> HttpResponse<Any>
): HttpResponse<Any> {
  val users = this.findAll()
  val user = users.firstOrNull { it.id == id?.toInt() && it.secret == secret }
  return if (user != null) {
    action(user)
  } else {
    HttpResponse.badRequest<Any>("No valid user session found")
  }
}