package io.saagie.croissants.domain

import javax.persistence.Id;
import java.time.Instant
import java.util.*
import javax.persistence.Entity


@Entity
data class User(
        var id: String = "",
        var username: String? = null,
        @Id
        var email: String? = null,
        var image_24: String? = null,
        var image_32: String? = null,
        var image_48: String? = null,
        var image_72: String? = null,
        var image_192: String? = null,
        var image_512: String? = null,
        var creationDate: Date = Date.from(Instant.now()),
        var coefficient: Int = 1,
        var initialWeight: Int = 0,
        var enable: Boolean = false
) {

    fun status(): String {
        when {
            (enable) -> return "Active"
            else -> return "Inactive"
        }
    }

    fun incrementCoefficient( inc: Int): User {
        this.coefficient = this.coefficient + inc
        return this
    }
}