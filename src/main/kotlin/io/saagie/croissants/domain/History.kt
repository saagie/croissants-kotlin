package io.saagie.croissants.domain

import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class History(

        @Id @GeneratedValue(strategy= GenerationType.AUTO)
        val id: String? = null,
        val dateCroissant: Date = Date.from(Instant.now()),
        val emailUser: String? = null,
        //Value of OK
        // 0 = Waiting answer
        // 1 = Accepted
        // 2 = Refused
        var ok: Int = 0,
        var dateDraw: Date = Date.from(Instant.now())
){

        fun setAccepted(): History{
                this.ok=1
                return this
        }
        fun setRefused(): History{
                this.ok=2
                return this

        }

}