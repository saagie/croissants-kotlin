package io.saagie.croissants.domain

import javax.persistence.Id;
import java.time.Instant
import java.util.*
import javax.persistence.Entity


@Entity
data class User(
        @Id
        val id: String?,
        val username: String,
        var email: String? = null,
        var image_24: String? = null,
        var image_32: String? = null,
        var image_48: String? = null,
        var image_72: String? = null,
        var image_192: String? = null,
        var image_512: String? = null,
        var creationDate: Date = Date.from(Instant.now()),
        var coefficient: Int = 0,
        var enable: Boolean = false,
        var activated: Boolean = false,
        var unregister: Boolean = false
) {
    fun status(): String {
        when {
            (enable && activated) -> return "Active"
            !activated -> return "Not activated"
            else -> return "Hibernate"
        }
    }

    fun incrementCoefficient( inc: Int) {
        this.coefficient = this.coefficient + inc
    }
}