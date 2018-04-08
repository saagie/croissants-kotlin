package io.saagie.croissants.service

import io.saagie.croissants.dao.HistoryDao
import io.saagie.croissants.dao.UserDao
import io.saagie.croissants.domain.History
import org.springframework.stereotype.Service

@Service
class HistoryService (
        val historyDao : HistoryDao,
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
        return historyDao.findAllByIdUser(userId)
    }
    fun getLastSelected(): History? {

        return historyDao.findAll().maxBy { it.dateCroissant }

    }

}