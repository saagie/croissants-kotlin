package io.saagie.astonparking.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import java.time.Instant
import java.util.*

data class User(
        @JsonIgnore @Id val id: String?,
        val username: String,
        var email: String? = null,
        var image_24: String? = null,
        var image_32: String? = null,
        var image_48: String? = null,
        var image_72: String? = null,
        var image_192: String? = null,
        var image_512: String? = null,
        var creationDate: Date = Date.from(Instant.now()),
        var attribution: Int = 0,
        var enable: Boolean = false,
        var activated: Boolean = false,
        var alreadySelected: Boolean = false,
        var hasFixedSpot: Boolean = false,
        var unregister: Boolean = false
) {
    fun status(): String {
        when {
            (enable && activated) -> return "Active"
            !activated -> return "Not activated"
            else -> return "Hibernate"
        }
    }

    fun incrementAttribution() {
        this.attribution = this.attribution + 1
    }
}