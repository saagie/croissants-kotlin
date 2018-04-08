package io.saagie.croissants.domain

import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class History(
        @Id
        val id: String? = null,
        val dateCroissant: Date = Date.from(Instant.now()),
        val idUser: String? = null,
        val ok: Int = 1
)