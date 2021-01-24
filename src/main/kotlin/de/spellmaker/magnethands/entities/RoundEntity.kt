package de.spellmaker.magnethands.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * A round of play in a room
 *
 * @param id the id of the round
 * @param room The room this round belongs to
 * @param snippets snippets of the round
 */
@Entity
@Table(name = "round_entity")
data class RoundEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  val id: Int? = null,
  @ManyToOne
  val room: RoomEntity,
  @OneToMany(mappedBy = "round", fetch = FetchType.EAGER)
  val snippets: List<SnippetEntity>
)