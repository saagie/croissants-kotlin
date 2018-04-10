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
        val emailUser: String? = null,
        //Value of OK
        // 0 = Waiting answer
        // 1 = Accepted
        // 2 = Refused
        var ok: Int = 0
){

        fun setAccepted(): History{
                this.ok=1
                return this
        }
        fun setRefused(): History{
                this.ok=1
                return this

        }

}