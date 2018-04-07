package io.saagie.croissants.domain

import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class History(
        @Id
        val id: String?,
        val dateCroissant: Date,
        val idUser: String?,
        val ok: Int = 1
)