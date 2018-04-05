package io.saagie.astonparking.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.saagie.astonparking.dao.PropositionDao
import io.saagie.astonparking.dao.RequestDao
import io.saagie.astonparking.dao.ScheduleDao
import io.saagie.astonparking.domain.*
import io.saagie.astonparking.slack.SlackBot
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.never
import java.lang.IllegalArgumentException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.fail


class DrawServiceTest {

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    val allUsers = initAllUser()

    val allSpots = initAllSpots()

    val allPropositions = initAllPropositions()

    val userService = mock<UserService> {
        on { getAllActive() } `it returns` allUsers
        on { get(allUsers.first().id!!) } `it returns` allUsers.first()
    }

    val spotService = mock<SpotService> {
        on { getAllSpots(State.FREE) } `it returns` allSpots.filter { it.state == State.FREE }
        on { getAllSpots(State.FIXED) } `it returns` allSpots.filter { it.state == State.FIXED }

    }

    val emailService = mock<EmailService> {

    }
    val propositionDao = mock<PropositionDao> {
        on { findAll() } `it returns` allPropositions
    }
    val scheduleDao = mock<ScheduleDao> {
        on { exists(any()) } `it returns` true
        on { findByDate(any()) } `it returns` Schedule(date = LocalDate.now(), assignedSpots = arrayListOf(), userSelected = arrayListOf(), freeSpots = arrayListOf())
    }

    val slackBot = mock<SlackBot> {

    }

    val requestDao = mock<RequestDao> {
        on { findByUserId("ID1") } `it returns` null
        on { findByUserId("ID2") } `it returns` listOf(Request(id = "RQ1", date = LocalDate.now(), userId = "ID2", submitDate = LocalDateTime.now()))
    }

    val drawService = DrawService(userService, spotService, emailService, slackBot, propositionDao, scheduleDao, requestDao)

    @Test
    fun should_return_the_list_of_active_users_in_the_right_order() {
        //Given
        //When
        val users = drawService.sortAndFilterUsers()
        //Then
        users.map { u -> u.attribution } shouldEqual listOf(0, 2, 3, 4)
    }

    @Test
    fun should_return_the_next_monday() {
        //Given
        val d1 = LocalDate.parse("2017-09-04")
        val d2 = LocalDate.parse("2017-09-05")
        val d3 = LocalDate.parse("2017-09-06")
        val d4 = LocalDate.parse("2017-09-07")
        val d5 = LocalDate.parse("2017-09-08")
        val d6 = LocalDate.parse("2017-09-09")
        val d7 = LocalDate.parse("2017-09-10")
        //When
        val nextMonday1 = drawService.getNextMonday(d1)
        val nextMonday2 = drawService.getNextMonday(d2)
        val nextMonday3 = drawService.getNextMonday(d3)
        val nextMonday4 = drawService.getNextMonday(d4)
        val nextMonday5 = drawService.getNextMonday(d5)
        val nextMonday6 = drawService.getNextMonday(d6)
        val nextMonday7 = drawService.getNextMonday(d7)
        //Then
        nextMonday1 `should equal` nextMonday2
        nextMonday1 `should equal` nextMonday3
        nextMonday1 `should equal` nextMonday4
        nextMonday1 `should equal` nextMonday5
        nextMonday1 `should equal` nextMonday6
        nextMonday1 `should equal` nextMonday7
        nextMonday1.dayOfWeek `should equal` DayOfWeek.MONDAY
        nextMonday1.isAfter(d1) `should equal` true
        nextMonday1.isAfter(d2) `should equal` true
        nextMonday1.isAfter(d3) `should equal` true
        nextMonday1.isAfter(d4) `should equal` true
        nextMonday1.isAfter(d5) `should equal` true
        nextMonday1.isAfter(d6) `should equal` true
        nextMonday1.isAfter(d7) `should equal` true
        nextMonday1.dayOfMonth `should equal` 11
        nextMonday1.monthValue `should equal` 9
        nextMonday1.year `should equal` 2017
    }

    @Test
    fun should_generate_list_of_propositions() {
        //Given
        val nextMonday = LocalDate.parse("2017-09-11")
        val id = "ID"
        val number = 1
        //When
        val generateAllProposition = drawService.generateAllProposition(number = number, userId = id, nextMonday = nextMonday)
        //Then
        generateAllProposition.size `should equal` 5
        generateAllProposition.map { it.day } `should equal` (
                arrayListOf(
                        LocalDate.parse("2017-09-11"),
                        LocalDate.parse("2017-09-12"),
                        LocalDate.parse("2017-09-13"),
                        LocalDate.parse("2017-09-14"),
                        LocalDate.parse("2017-09-15")
                )
                )
    }

    @Test
    fun should_make_attributions() {
        //Given
        //When
        drawService.attribution(null)
        //Then
        verify(propositionDao, times(1)).save(Mockito.anyListOf(Proposition::class.java))
        verify(emailService, times(1)).proposition(Mockito.anyListOf(Proposition::class.java), Mockito.anyListOf(User::class.java))
        verify(userService, times(3)).save(any())
    }

    @Test
    fun should_return_all_propositions() {
        //Given
        //When
        val propositions = drawService.getAllPropositions()
        //Then
        verify(propositionDao, times(1)).findAll()
        propositions shouldEqual allPropositions
    }

    @Test
    fun should_accept_propositions() {
        //Given
        //When
        val response = drawService.acceptProposition(allUsers.first().id!!)
        //Then
        response `should be` true
        verify(scheduleDao, times(2)).save(Mockito.any(Schedule::class.java))
        verify(propositionDao, times(2)).delete(Mockito.anyString())
    }

    @Test
    fun should_add_request() {
        //Given
        //When
        val date = LocalDate.now().plusDays(1)
        drawService.request("ID1", "${date.format(DateTimeFormatter.ofPattern("dd/MM"))}")
        //Then
        verify(requestDao, times(1)).save(Mockito.any(Request::class.java))
    }

    @Test
    fun should_not_add_request() {
        //Given
        //When
        try {
            val date = LocalDate.now().minusDays(1)
            drawService.request("ID2", "${date.format(DateTimeFormatter.ofPattern("dd/MM"))}")
            fail("Should return an exception")
        } catch (e: IllegalArgumentException) {
            verify(requestDao, never()).save(Mockito.any(Request::class.java))
        }
    }

    @Test
    fun should_reset_user_counter() {
        //Given
        //When
        drawService.resetAllScores()
        //Then
        verify(userService, times(1)).saveall(any())
    }

    private fun initAllUser(): List<User> {
        return arrayListOf<User>(
                User(
                        id = "ID1",
                        username = "User1",
                        email = "mail1@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 0,
                        enable = true,
                        activated = true),
                User(
                        id = "ID3",
                        username = "User3",
                        email = "mail3@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 3,
                        enable = true,
                        activated = false),
                User(
                        id = "ID2",
                        username = "User2",
                        email = "mail2@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 2,
                        enable = true,
                        activated = true),
                User(
                        id = "ID4",
                        username = "User4",
                        email = "mail4@mail.com",
                        creationDate = Date.from(Instant.now()),
                        attribution = 4,
                        enable = true,
                        activated = true)
        )
    }

    private fun initAllSpots(): List<Spot> {
        return listOf(
                Spot(id = "SPOT0", number = 100, state = State.FREE, userId = null),
                Spot(id = "SPOT1", number = 101, state = State.FREE, userId = null),
                Spot(id = "SPOT2", number = 102, state = State.FREE, userId = null),
                Spot(id = "SPOT3", number = 103, state = State.FIXED, userId = "ID1"),
                Spot(id = "SPOT4", number = 104, state = State.FIXED, userId = null)
        )
    }

    private fun initAllPropositions(): ArrayList<Proposition> {
        return arrayListOf(
                Proposition(id = "PID1", userId = allUsers.get(0).id!!, spotNumber = allSpots.get(0).number, day = LocalDate.now()),
                Proposition(id = "PID2", userId = allUsers.get(0).id!!, spotNumber = allSpots.get(1).number, day = LocalDate.now()),
                Proposition(id = "PID3", userId = allUsers.get(1).id!!, spotNumber = allSpots.get(2).number, day = LocalDate.now())
        )
    }

}
