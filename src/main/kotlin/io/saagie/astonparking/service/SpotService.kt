package io.saagie.astonparking.service

import io.saagie.astonparking.dao.SpotDao
import io.saagie.astonparking.domain.Spot
import io.saagie.astonparking.domain.State
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class SpotService(val spotDao: SpotDao) {

    fun getAllSpots(state: State): List<Spot>? {
        return getAllSpots(state.name)
    }

    fun getAllSpots(state: String?): List<Spot>? {
        when (state) {
            State.FIXED.name, State.FREE.name -> return spotDao.findByState(State.valueOf(state))
            null -> return spotDao.findAll() as List<Spot>
            else -> throw IllegalArgumentException("State is unknown")
        }
    }

    fun getSpot(number: Int): Spot? {
        return spotDao.findByNumber(number)
    }

    fun updateSpot(number: Int, state: State): Spot? {
        val spot = spotDao.findByNumber(number)
        spot?.state = state
        spotDao.save(spot!!)
        return spot
    }

    fun createSpot(spot: Spot): Spot {
        return spotDao.save(spot)
    }

    fun deleteSpot(number: Int) {
        val spot = spotDao.findByNumber(number)
        spotDao.delete(spot!!)
    }
}