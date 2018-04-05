package io.saagie.astonparking.service

import io.saagie.astonparking.dao.PropositionDao
import io.saagie.astonparking.dao.RequestDao
import io.saagie.astonparking.dao.ScheduleDao
import io.saagie.astonparking.domain.*
import io.saagie.astonparking.slack.SlackBot
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class DrawService(
        val userService: UserService,
        val spotService: SpotService,
        val emailService: EmailService,
        val slackBot: SlackBot,
        val propositionDao: PropositionDao,
        val scheduleDao: ScheduleDao,
        val requestDao: RequestDao
) {

    @Scheduled(cron = "0 0 8 * * MON")
    fun scheduleAttribution() {
        userService.resetAllSelectedAttribution()
        propositionDao.deleteAll()
        this.attribution(null)
        this.fixedSpots()
    }

    @Scheduled(cron = "0 0 7 * * MON")
    fun removeUnregisterUser() {
        val users = userService.findByUnregister(true)
        users.forEach {
            val userId = it.id
            userService.delete(userId!!)
            val date = LocalDate.now()
            val schedules = scheduleDao.findByDateIn(listOf(date, date.plusDays(1), date.plusDays(2), date.plusDays(3), date.plusDays(4)))
            schedules.filter({ it.assignedSpots.map { it.userId }.contains(userId) }).forEach {
                val dateFormat = date.format(DateTimeFormatter.ofPattern("dd/MM"))
                release(userId, dateFormat)
            }
            scheduleDao.save(schedules)
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    fun resetRequest() {
        requestDao.deleteByDateBefore(LocalDate.now())
    }

    @Scheduled(cron = "0 0 8 * * SUN")
    fun cleanAttributions() {
        val propositions = propositionDao.findAll()
        propositions.forEach({
            var schedule = Schedule(
                    date = it.day,
                    assignedSpots = arrayListOf(),
                    freeSpots = arrayListOf(),
                    userSelected = arrayListOf()
            )
            if (scheduleDao.exists(it.day)) {
                schedule = scheduleDao.findOne(it.day)
            }
            schedule.freeSpots.add(it.spotNumber)
            scheduleDao.save(schedule)
        })
        propositionDao.deleteAll()
    }

    @Scheduled(cron = "0 0 0 1 6 *")
    fun resetAllScores() {
        val allUsers = userService.getAll()
        userService.saveall(allUsers.map { it.apply { attribution = 0 } })
    }

    @Async
    fun attribution(spotNumber: Int?) {
        val sortedActiveUsers = sortAndFilterUsers().filter { it.alreadySelected == false }
        val nextMonday = getNextMonday(LocalDate.now())
        var availableSpots = spotService.getAllSpots(State.FREE)
        if (spotNumber != null) {
            availableSpots = availableSpots!!.filter { it.number == spotNumber }
        }
        val userIterator = sortedActiveUsers.iterator()
        val propositions = arrayListOf<Proposition>()
        availableSpots!!.forEach {
            if (userIterator.hasNext()) {
                val user = userIterator.next()
                propositions.addAll(generateAllProposition(it.number, user.id!!, nextMonday))
                user.alreadySelected = true
                userService.save(user)
            }
        }
        propositionDao.save(propositions)
        emailService.proposition(propositions, sortedActiveUsers)
        slackBot.proposition(propositions, sortedActiveUsers, nextMonday)
    }

    fun fixedSpots() {
        val nextMonday = getNextMonday(LocalDate.now())
        val fixedSpots = spotService.getAllSpots(State.FIXED)?.filter { it.userId != null }

        fixedSpots?.forEach {
            val user = userService.get(it.userId!!)
            if (user.activated) {
                for (i in 0L..4L) {
                    val date = nextMonday.plusDays(i)
                    var schedule = Schedule(
                            date = date,
                            assignedSpots = arrayListOf(),
                            freeSpots = arrayListOf(),
                            userSelected = arrayListOf()
                    )
                    if (scheduleDao.exists(date)) {
                        schedule = scheduleDao.findOne(date)
                    }
                    schedule.userSelected.add(it.userId!!)
                    schedule.assignedSpots.add(
                            ScheduleSpot(
                                    spotNumber = it.number,
                                    userId = it.userId!!,
                                    username = user.username,
                                    acceptDate = LocalDateTime.now())
                    )
                    scheduleDao.save(schedule)
                }
            }
        }
    }

    fun generateAllProposition(number: Int, userId: String, nextMonday: LocalDate): List<Proposition> {
        val listProps = arrayListOf<Proposition>()
        for (i in 0L..4L) {
            if (!spotAlreadyProposed(number, nextMonday.plusDays(i)) &&
                    !spotAlreadySchedule(number, nextMonday.plusDays(i))) {
                listProps.add(
                        Proposition(
                                spotNumber = number,
                                userId = userId,
                                day = nextMonday.plusDays(i)
                        ))
            }
        }
        return listProps
    }

    private fun spotAlreadyProposed(number: Int, date: LocalDate?): Boolean {
        val propositions = propositionDao.findAll()
        return propositions != null && propositions.filter { it.spotNumber == number && it.day == date }.isNotEmpty()
    }

    private fun spotAlreadySchedule(number: Int, date: LocalDate): Boolean {
        val schedule = scheduleDao.findByDate(date)
        return schedule != null && schedule.assignedSpots.filter { it.spotNumber == number }.isNotEmpty()

    }

    fun getNextMonday(d: LocalDate): LocalDate {
        return d.plusDays((8 - d.dayOfWeek.value).toLong())
    }

    fun sortAndFilterUsers(): List<User> {
        return userService
                .getAllActive()
                .filter { !it.hasFixedSpot }
                .sortedBy { it.attribution }
    }

    fun getAllPropositions(): ArrayList<Proposition>? {
        return propositionDao.findAll() as ArrayList<Proposition>?

    }

    fun acceptProposition(userId: String): Boolean {
        val propositions = propositionDao.findAll()
        val user = userService.get(userId)
        val filteredProposition = propositions.filter { it.userId == userId }
        if (filteredProposition.isNotEmpty()) {
            acceptAllPropositions(filteredProposition, user)
            return true
        }
        return false
    }

    @Async
    fun acceptAllPropositions(filteredProposition: List<Proposition>, user: User) {
        filteredProposition.forEach {
            var schedule = Schedule(
                    date = it.day,
                    assignedSpots = arrayListOf(),
                    freeSpots = arrayListOf(),
                    userSelected = arrayListOf()
            )
            if (scheduleDao.exists(it.day)) {
                schedule = scheduleDao.findByDate(it.day)!!
            }
            schedule.userSelected.add(user.id!!)
            val spotNumber = it.spotNumber
            if (schedule.assignedSpots.count { it.spotNumber == spotNumber } == 0) {
                schedule.assignedSpots.add(
                        ScheduleSpot(
                                spotNumber = spotNumber,
                                userId = user.id,
                                username = user.username,
                                acceptDate = LocalDateTime.now())
                )
            }
            scheduleDao.save(schedule)
            propositionDao.delete(it.id!!)
            user.incrementAttribution()
        }
        userService.save(user)
    }

    @Async
    fun declineProposition(userId: String) {
        val propositions = propositionDao.findAll()
        val props = propositions.filter { it.userId == userId }
        propositionDao.delete(props)
        this.attribution(props.first().spotNumber)
    }

    fun getCurrentSchedules(): List<Schedule> {
        return getSchedules(this.getNextMonday(LocalDate.now()).minusDays(7))
    }

    fun getNextSchedules(): List<Schedule> {
        return getSchedules(this.getNextMonday(LocalDate.now()))
    }

    fun getSchedules(date: LocalDate): List<Schedule> {
        return scheduleDao.findByDateIn(listOf(date, date.plusDays(1), date.plusDays(2), date.plusDays(3), date.plusDays(4)))
    }

    @Async
    fun release(userId: String, text: String) {
        val date = extractDate(text)
        val user = userService.get(userId)
        val schedule = scheduleDao.findByDate(date)
        val spotToBeDeleted = schedule!!.assignedSpots.filter { it.userId == userId }
        schedule.assignedSpots.removeAll(spotToBeDeleted)
        if (spotToBeDeleted.isNotEmpty()) {
            schedule.freeSpots.add(spotToBeDeleted.first().spotNumber)
        }
        scheduleDao.save(schedule)
        if (releaseIsOkToDecrementAttribution(date)) {
            user.attribution = user.attribution - 1
            userService.save(user)
        }
        if (!checkAndPickIfRequest(date)) {
            slackBot.spotRelease(date)
        }
    }

    private fun releaseIsOkToDecrementAttribution(date: LocalDate): Boolean {
        val now = LocalDate.now()
        val localDateTime = LocalDateTime.now()
        when {
            now != date -> return true
            else -> return (localDateTime.hour < 8)
        }
    }

    private fun checkAndPickIfRequest(date: LocalDate): Boolean {
        val request = requestDao.findByDate(date)
        if (request != null && request.isNotEmpty()) {
            val winner = request.sortedBy { it.submitDate }.first()
            val spot = pick(winner.userId, date)
            val user = userService.get(winner.userId)
            user.attribution += 1
            userService.save(user)
            requestDao.delete(winner.id!!)
            emailService.pickAfterRequest(userService.get(winner.userId), spot, date)
            return true
        }
        return false
    }

    fun extractDate(text: String): LocalDate {
        val now = LocalDate.now()
        try {
            val date = LocalDate.parse(text + "/${now.year}", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            if (now.isAfter(date)) {
                throw IllegalArgumentException("Date can't be before today")
            }
            return date
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Date format is not correct : dd/MM (day/month)")
        }
    }

    fun pick(userId: String, date: LocalDate): Int {
        val user = userService.get(userId)
        val schedule = scheduleDao.findByDate(date)
                ?: throw IllegalArgumentException("No schedule for the date ${date}")
        if (schedule.freeSpots.isEmpty())
            throw IllegalArgumentException("No free spot for the date ${date}")
        if (schedule.assignedSpots.count { it.userId == userId } > 0)
            throw IllegalArgumentException("A spot is already reserved for you")
        val freeSpot = schedule.freeSpots.first()
        schedule.freeSpots.removeAt(0)
        if (!schedule.userSelected.contains(user.id)) {
            schedule.userSelected.add(user.id!!)
        }
        schedule.assignedSpots.add(
                ScheduleSpot(
                        spotNumber = freeSpot,
                        userId = user.id!!,
                        username = user.username,
                        acceptDate = LocalDateTime.now())
        )
        scheduleDao.save(schedule)
        user.attribution = user.attribution + 1
        userService.save(user)
        return freeSpot
    }

    fun pick(userId: String, text: String) = pick(userId, extractDate(text))

    fun request(userId: String, text: String) {
        val date = extractDate(text)

        val requests = requestDao.findByUserId(userId)
        if (requests != null && requests.isNotEmpty()) {
            throw IllegalArgumentException("You have a request already recorded.")
        }
        requestDao.save(
                Request(
                        userId = userId,
                        date = date,
                        submitDate = LocalDateTime.now())
        )
        slackBot.requestCreated(userService.get(userId), date)
    }

    fun cancelrequest(userId: String) {
        val request = requestDao.findByUserId(userId)
        if (request != null && request.isNotEmpty()) {
            requestDao.delete(request.first().id)
        }
    }
}