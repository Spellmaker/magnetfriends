package de.spellmaker.magnethands.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * A room for games
 *
 * @param id the id of the entity
 * @param name a label for the room
 * @param rounds The rounds of the room
 * @param owner the room creator
 * @param members people playing in the room
 */
@Entity
@Table(name = "room_entity")
data class RoomEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  val id: Int? = null,
  @Column
  val name: String,
  @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
  val rounds: List<RoundEntity>,
  @ManyToOne
  val owner: UserEntity,
  @ManyToMany(fetch = FetchType.EAGER)
  val members: Set<UserEntity>
)