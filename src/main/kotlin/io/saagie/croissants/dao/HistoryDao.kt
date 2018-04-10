package io.saagie.croissants.dao

import io.saagie.croissants.domain.History
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HistoryDao : CrudRepository<History, String> {
    fun findAllByEmailUser(emailUser: String?): List<History>
    fun findByDateCroissant(dateCroissant: Date): History
}