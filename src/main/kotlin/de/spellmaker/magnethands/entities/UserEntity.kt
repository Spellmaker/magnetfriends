package de.spellmaker.magnethands.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

/**
 * A user of the application
 *
 * @param id The id to use
 * @param secret The user secret for re-authentication
 * @param name The display name of the user
 */
@Table(name = "user_entity")
@Entity
data class UserEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  val id: Int? = null,
  @Column
  @JsonIgnore
  val secret: String,
  @Column
  val name: String
)