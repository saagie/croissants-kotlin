package io.saagie.astonparking.service

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import io.saagie.astonparking.dao.SpotDao
import io.saagie.astonparking.domain.Spot
import io.saagie.astonparking.domain.State
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Matchers
import org.mockito.Mockito.*
import java.lang.IllegalArgumentException
import kotlin.test.fail

class SpotServiceTest {

    val allSpots = initAllSpots()

    val spotCaptor = ArgumentCaptor.forClass(Spot::class.java)

    val spotDao: SpotDao = mock<SpotDao> {
        on { findAll() }.doReturn(allSpots)
        on { findByState(State.FIXED) }.doReturn(allSpots.filter { it.state == State.FIXED })
        on { findByState(State.FREE) }.doReturn(allSpots.filter { it.state == State.FREE })
        on { findByNumber(allSpots.first().number) }.doReturn(allSpots.first())
        on { save(Matchers.any(Spot::class.java)) }.doReturn(newSpot())
    }

    val spotService = SpotService(spotDao)

    @Test
    fun should_return_all_spot_when_no_state_filter() {
        // Given
        // When
        val returnedAllSpots = spotService.getAllSpots(null)
        // Then
        verify(spotDao).findAll()
        verify(spotDao, never()).findByState(State.FREE)
        verify(spotDao, never()).findByState(State.FIXED)
        returnedAllSpots `should equal` allSpots
    }

    @Test
    fun should_return_an_exception_when_an_invalid_state_is_set() {
        // Given
        // When
        try {
            spotService.getAllSpots("UNKNOWN STATE")
            fail("Should return an exception if the state is unknown")
        } catch (e: IllegalArgumentException) {
            e.message `should be` "State is unknown"
        }
        // Then
        verify(spotDao, never()).findAll()
        verify(spotDao, never()).findByState(State.FREE)
        verify(spotDao, never()).findByState(State.FIXED)
    }

    @Test
    fun should_return_filtered_spot_when_no_state_is_set() {
        // Given
        // When
        val returnedAllSpotsWithFixed = spotService.getAllSpots(State.FIXED.name)
        val returnedAllSpotsWithFixedUsingEnum = spotService.getAllSpots(State.FIXED)
        val returnedAllSpotsWithFree = spotService.getAllSpots(State.FREE.name)
        val returnedAllSpotsWithFreeUsingEnum = spotService.getAllSpots(State.FREE)
        // Then
        verify(spotDao, never()).findAll()
        verify(spotDao, times(2)).findByState(State.FREE)
        verify(spotDao, times(2)).findByState(State.FIXED)
        returnedAllSpotsWithFixed `should equal` allSpots.filter { it.state == State.FIXED }
        returnedAllSpotsWithFixed `should equal` returnedAllSpotsWithFixedUsingEnum
        returnedAllSpotsWithFree `should equal` allSpots.filter { it.state == State.FREE }
        returnedAllSpotsWithFree `should equal` returnedAllSpotsWithFreeUsingEnum
    }


    @Test
    fun should_return_a_spot_when_number_exists() {
        //Given
        //When
        val spot = spotService.getSpot(allSpots.first().number)
        //Then
        verify(spotDao, times(1)).findByNumber(100)
        spot `should be` allSpots.first()
    }

    @Test
    fun should_return_null_when_number_doesnt_exists() {
        //Given
        //When
        val spot = spotService.getSpot(0)
        //Then
        verify(spotDao, never()).findByNumber(100)
        spot `should be` null
    }


    @Test
    fun should_update_spot() {
        //Given
        //When
        val updatedSpot = spotService.updateSpot(100, State.FREE)
        //Then
        updatedSpot?.state `should be` State.FREE
        verify(spotDao, times(1)).save(spotCaptor.capture())
        spotCaptor.value.number `should be` 100
        spotCaptor.value.state `should be` State.FREE
    }

    @Test
    fun should_create_a_new_spot() {
        //Given
        val spot = newSpot()
        //When
        val createdSpot = spotService.createSpot(spot)
        //Then
        verify(spotDao, times(1)).save(spotCaptor.capture())
        spotCaptor.value.state `should be` State.FREE
        spotCaptor.value.number `should be` 104
        createdSpot `should equal` spot
    }

    @Test
    fun should_delete_spot() {
        //Given
        //When
        spotService.deleteSpot(100)
        //Then
        verify(spotDao, times(1)).delete(spotCaptor.capture())
        verify(spotDao, times(1)).findByNumber(100)
        spotCaptor.value.number `should be` 100
    }

    private fun newSpot() = Spot(null, 104, State.FREE,userId = null)

    private fun initAllSpots(): List<Spot> {
        return arrayListOf<Spot>(
                Spot(null, 100, State.FIXED,userId = null),
                Spot(null, 101, State.FIXED,userId = "1"),
                Spot(null, 102, State.FIXED,userId = "2"),
                Spot(null, 103, State.FREE,userId = null)
        )
    }
}
