package de.spellmaker.magnethands.entities

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * A snippet put into the basket
 *
 * @param id the id of the snipped
 * @param content the display content
 * @param holder The user holding the snippet, if any
 * @param round The round this snippet is associated with
 * @param creator The submitter of this snippet
 * @param isShown if the snippet is currently displayed to all participants
 * @param isUsed if the snippet has been used already
 */
@Table(name = "snippet_entity")
@Entity
data class SnippetEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  val id: Int? = null,
  @Column
  val content: String,
  @ManyToOne
  @NotFound(action = NotFoundAction.IGNORE)
  val holder: UserEntity?,
  @ManyToOne
  val round: RoundEntity,
  @ManyToOne
  val creator: UserEntity,
  @Column
  val isShown: Boolean = false,
  @Column
  val isUsed: Boolean = false
)