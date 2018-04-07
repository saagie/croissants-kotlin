package io.saagie.croissants.dao

import io.saagie.croissants.domain.History
import io.saagie.croissants.domain.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

interface HistoryDao : CrudRepository<History, String> {
    fun findAllByIdUser(idUser: String?): List<History>
}