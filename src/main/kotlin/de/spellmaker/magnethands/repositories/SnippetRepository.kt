package de.spellmaker.magnethands.repositories

import de.spellmaker.magnethands.entities.SnippetEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

/**
 * repository for snippets
 */
@Repository
interface SnippetRepository : CrudRepository<SnippetEntity, Int>