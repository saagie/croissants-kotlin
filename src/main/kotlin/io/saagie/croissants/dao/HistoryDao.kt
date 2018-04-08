package io.saagie.croissants.dao

import io.saagie.croissants.domain.History
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoryDao : CrudRepository<History, String> {
    fun findAllByIdUser(idUser: String?): List<History>
}