package de.spellmaker.magnethands.repositories

import de.spellmaker.magnethands.entities.UserEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

/**
 * Repository for user instances
 */
@Repository
interface UserRepository: CrudRepository<UserEntity, Int>