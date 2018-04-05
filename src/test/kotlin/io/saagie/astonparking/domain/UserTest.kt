package io.saagie.astonparking.domain

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqualTo
import org.junit.Test


class UserTest {

    @Test
    fun should_status_is_active() {
        //Given
        val user = User(
                id = "ID1",
                username = "Test User",
                activated = true,
                enable = true
        )
        //When
        val status = user.status()
        //Then
        status shouldBeEqualTo "Active"
    }

    @Test
    fun should_status_is_inactive() {
        //Given
        val user = User(
                id = "ID1",
                username = "Test User",
                activated = true,
                enable = false
        )
        //When
        val status = user.status()
        //Then
        status shouldBeEqualTo "Hibernate"
    }

    @Test
    fun should_status_is_not_activated_with_enable_at_false() {
        //Given
        val user = User(
                id = "ID1",
                username = "Test User",
                activated = false,
                enable = false
        )
        //When
        val status = user.status()
        //Then
        status shouldBeEqualTo "Not activated"
    }

    @Test
    fun should_status_is_not_activated_with_enable_at_true() {
        //Given
        val user = User(
                id = "ID1",
                username = "Test User",
                activated = false,
                enable = true
        )
        //When
        val status = user.status()
        //Then
        status shouldBeEqualTo "Not activated"
    }

    @Test
    fun should_increment_attribution() {
        //Given
        val user = User(
                id = "ID1",
                username = "Test User",
                activated = false,
                enable = true,
                attribution = 0
        )
        //When
        user.incrementAttribution()
        //Then
        user.attribution shouldEqualTo 1

    }
}