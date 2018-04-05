package io.saagie.astonparking.dao

import io.saagie.astonparking.domain.Request
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate

interface RequestDao : MongoRepository<Request, String> {
    fun findByUserId(userId: String): List<Request>?
    fun deleteByDateBefore(date: LocalDate)
    fun findByDate(date: LocalDate): List<Request>?
}