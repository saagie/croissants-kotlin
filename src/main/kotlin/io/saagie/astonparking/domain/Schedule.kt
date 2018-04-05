package io.saagie.astonparking.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

@Document
data class Schedule(
        @Id val date: LocalDate,
        val assignedSpots: ArrayList<ScheduleSpot>,
        val freeSpots: ArrayList<Int>,
        val userSelected: ArrayList<String>
)

data class ScheduleSpot(
        val spotNumber: Int,
        val userId: String,
        val username: String,
        val acceptDate: LocalDateTime
)