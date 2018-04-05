package io.saagie.astonparking.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Spot(
        @JsonIgnore @Id val id: String?,
        val number: Int,
        var state: State,
        var userId: String?
)