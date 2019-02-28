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

    fun getLastYearByEmailUser(userEmail: String): List<History> {
        return historyDao.findAllByEmailUser(userEmail).filter {
            (it.dateCroissant > Date.from(Instant.now().minus(365, ChronoUnit.DAYS)) && it.dateCroissant < Date.from(Instant.now()) && it.ok == 1 )  }
    }

    fun getLastSelected(): History? {
        return historyDao.findAll().filter { it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant <= utilService.localDateToDate(utilService.getNextFriday()) && it.ok == 1 }.firstOrNull()
    }

    fun getNextSelected(): List<History?> {
        return historyDao.findAll().filter { it.dateCroissant > Date.from(Instant.now()) && it.ok == 1}.sortedBy { it.dateCroissant }
    }

    fun getByDate(date: Date): List<History> {

        return historyDao.findAll().filter { it.dateCroissant > utilService.localDateToDate( utilService.dateToLocalDate(date).minusDays(1)) &&  it.dateCroissant <= utilService.localDateToDate( utilService.dateToLocalDate(date))  }.sortedBy { it.id }

    }

    fun getByDrawDate(date: Date): List<History> {
        return getByDate(utilService.localDateToDate(utilService.getNextFriday())).filter { it.ok == 0  && it.dateDraw < date }
    }

    //on prend en compte aussi les 3 semaines dans le futur pour exclure les personnes qui se seraient proposées
    // pour respecter la règle : "on ne peut être tiré au sort pour les croissants qu'une fois toutes les 3 semaines"
    //on exclut également des retirages de la semaine les personnes ayant déjà décliné
    fun getAllExcludedHistory(): List<History> {
        return getAll().filter {
            (it.dateCroissant > Date.from(Instant.now().minus(21, ChronoUnit.DAYS)) && it.dateCroissant < Date.from(Instant.now().plus(21, ChronoUnit.DAYS)) && it.ok == 1 ) || ( it.dateCroissant > Date.from(Instant.now()) && it.dateCroissant <= utilService.localDateToDate(utilService.getNextFriday()) && it.ok == 2 ) }

    }


    fun purpose(userId: String, localdate: LocalDate): Boolean {
        val user = userDao.findOneById(userId)
        if (historyDao.findByDateCroissant(utilService.localDateToDate(localdate)).filter { it.ok !=2 }.isEmpty()){
            save(History(dateCroissant = utilService.localDateToDate(localdate), emailUser = user.email, ok = 1))
            return true
        }
        return false
    }

    fun save(history: History) {
        historyDao.save(history)
    }
    fun delete(history: History) {
        historyDao.delete(history)
    }
}