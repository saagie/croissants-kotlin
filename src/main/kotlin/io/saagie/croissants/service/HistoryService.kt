package io.saagie.croissants.service

import io.saagie.croissants.dao.HistoryDao
import io.saagie.croissants.dao.UserDao
import io.saagie.croissants.domain.History
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Service
class HistoryService(
        val historyDao: HistoryDao,
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

    fun getAllByUser(userId: String): List<History> {
        return historyDao.findAllByEmailUser(userId)
    }

    fun getLastSelected(): History? {

        return historyDao.findAll().maxBy { it.dateCroissant }

    }

    fun acceptSelection(userId: String): Boolean {
        return true
    }

    fun declineSelection(userId: String): Boolean {
        return true

    }

    fun purpose(userId: String, date: LocalDate): Boolean {
        return true

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

}