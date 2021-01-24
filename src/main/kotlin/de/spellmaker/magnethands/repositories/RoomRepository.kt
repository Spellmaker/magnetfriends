package de.spellmaker.magnethands.repositories

import de.spellmaker.magnethands.entities.RoomEntity
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository

/**
 * repository for rooms
 */
@Repository
interface RoomRepository: CrudRepository<RoomEntity, Int>