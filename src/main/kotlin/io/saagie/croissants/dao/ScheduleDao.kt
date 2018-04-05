package io.saagie.croissants.dao

import io.saagie.croissants.domain.Schedule
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ScheduleDao : MongoRepository<Schedule, LocalDate> {
    fun findByDateIn(dates: List<LocalDate>): List<Schedule>
    fun findByDate(date: LocalDate): Schedule?
}