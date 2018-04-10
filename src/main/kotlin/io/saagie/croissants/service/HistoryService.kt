package io.saagie.croissants.service

import io.saagie.croissants.dao.HistoryDao
import io.saagie.croissants.dao.UserDao
import io.saagie.croissants.domain.History
import io.saagie.croissants.slack.SlackBot
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class HistoryService(
        val historyDao: HistoryDao,
        val utilService: UtilService,
        //      val drawService: DrawService,
        val slackBot: SlackBot,
        val userDao: UserDao) {

    fun get(id: String): History {
        if (historyDao.exists(id)) {
            return historyDao.findOne(id)
        }
        throw IllegalArgumentException("History (id:${id}) not found")
    }

    fun getAll(): List<History> {
        return historyDao.findAll() as List<History>
    }

    fun getAllByEmailUser(userEmail: String): List<History> {
        return historyDao.findAllByEmailUser(userEmail)
    }


    fun getLastSelected(): History? {
        return historyDao.findAll().filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant <= utilService.localDateToDate(utilService.getNextFriday()) }.firstOrNull()
    }

    fun getByDate(date: Date): History {
        return historyDao.findByDateCroissant(date)
    }

    //on prend en compte aussi les 3 semaines dans le futur pour exclure les personnes qui se seraient proposées
    // pour respecter la règle : "on ne peut être tiré au sort pour les croissants qu'une fois toutes les 3 semaines"
    fun getAllHistoryOfLast3Weeks(): List<History> {
//        return historyService.getAll().filter { it.dateCroissant > Date.from(Instant.now().minus(21, ChronoUnit.DAYS)) }
        return getAll().filter { it.dateCroissant > Date.from(Instant.now().minus(21, ChronoUnit.DAYS)) && it.dateCroissant < Date.from(Instant.now().plus(21, ChronoUnit.DAYS)) }
    }

    fun acceptSelection(userId: String): Boolean {
        var history = getAllByEmailUser(userDao.findOneById(userId).email!!).filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant < Date.from(Instant.now().plus(3, ChronoUnit.DAYS)) && it.ok == 0 }
        if (history.isEmpty()) return false
        save(history.first().setAccepted())
        slackBot.selection(userDao.findOneById(userId), utilService.getNextFriday())
        return true
    }

    fun declineSelection(userId: String): Boolean {

        var history = getAllByEmailUser(userDao.findOneById(userId).email!!).filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant < Date.from(Instant.now().plus(3, ChronoUnit.DAYS)) && it.ok == 0 }
        if (history.isEmpty()) return false
        save(history.first().setRefused())
        userDao.save(userDao.findOneById(userId).incrementCoefficient(20))
        //TODO uncomment drawService.scheduleSelection()
        return true

    }

    fun purpose(userId: String, localdate: LocalDate): Boolean {
        val user = userDao.findOneById(userId)
        save(History(dateCroissant = utilService.localDateToDate(localdate), emailUser = user.email, ok = 1))
        return true

    }

    fun save(history: History) {
        historyDao.save(history)
    }
}