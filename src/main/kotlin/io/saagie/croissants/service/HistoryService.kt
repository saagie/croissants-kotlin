package io.saagie.croissants.service

import io.saagie.croissants.dao.HistoryDao
import io.saagie.croissants.dao.UserDao
import io.saagie.croissants.domain.History
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class HistoryService(
        val historyDao: HistoryDao,
        val utilService: UtilService,
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

    fun getAllByIdUser(userId: String): List<History> {

        return getAllByEmailUser(userDao.findOneById(userId).email!!)
    }

    fun getLastSelected(): History? {

        return historyDao.findAll().filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant < utilService.localDateToDate(utilService.getNextFriday()) }.firstOrNull()
    }

    fun acceptSelection(userId: String): Boolean {
        var history = getAllByIdUser(userId).filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant < Date.from(Instant.now().plus(3, ChronoUnit.DAYS)) }
       if (history.isEmpty()) return false
        save(history.first().setAccepted())
        return true
    }

    fun declineSelection(userId: String): Boolean {

        var history = getAllByIdUser(userId).filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant < Date.from(Instant.now().plus(3, ChronoUnit.DAYS)) }
        if (history.isEmpty()) return false
        save(history.first().setRefused())
        return true

    }

    fun purpose(userId: String, localdate: LocalDate): Boolean {
        val user = userDao.findOneById(userId)
        save(History(dateCroissant =  utilService.localDateToDate(localdate) , emailUser = user.email, ok = 1))
        return true

    }

    fun save(history: History) {
        historyDao.save(history)
    }
}