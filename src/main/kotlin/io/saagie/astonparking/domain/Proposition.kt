package io.saagie.astonparking.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document
data class Proposition(
        @JsonIgnore @Id val id: String? = null,
        val spotNumber: Int,
        val userId: String,
        val day: LocalDate
)