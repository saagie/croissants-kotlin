package io.saagie.croissants.dao

import io.saagie.croissants.domain.Spot
import io.saagie.croissants.domain.State
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpotDao : CrudRepository<Spot, String> {

    fun findByState(state: State): List<Spot>?
    fun findByNumber(number: Int): Spot?
}