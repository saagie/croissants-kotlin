package io.saagie.astonparking.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime

@Document
data class Request (
        @Id val id: String? = null,
        val date: LocalDate,
        val userId: String,
        val submitDate: LocalDateTime
)