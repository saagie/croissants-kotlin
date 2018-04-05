package io.saagie.astonparking.dao

import io.saagie.astonparking.domain.Spot
import io.saagie.astonparking.domain.State
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpotDao : CrudRepository<Spot, String> {

    fun findByState(state: State): List<Spot>?
    fun findByNumber(number: Int): Spot?
}