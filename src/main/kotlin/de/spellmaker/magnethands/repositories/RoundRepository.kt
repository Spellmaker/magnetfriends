package de.spellmaker.magnethands.repositories

import de.spellmaker.magnethands.entities.RoundEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

/**
 * repository for round instances
 */
@Repository
interface RoundRepository: CrudRepository<RoundEntity, Int>