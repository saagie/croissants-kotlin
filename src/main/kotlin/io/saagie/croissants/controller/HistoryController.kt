package io.saagie.croissants.controller

import io.saagie.croissants.domain.History
import io.saagie.croissants.service.HistoryService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Calendar



@RestController
class HistoryController(val historyService: HistoryService) {

    @GetMapping("/history")
    fun getAllHistory(): List<History> {
        return historyService.getAll()
    }

    @GetMapping("/history/{id}")
    fun getAllHistory(@PathVariable(name = "id", required = true) id: String): History {
        return historyService.get(id)
    }

    @GetMapping("/user/{user_id}/history")
    fun getAllHistoryByUser(@PathVariable(name = "user_id", required = true) user_id: String): List<History> {
        return historyService.getAllByUser(user_id)
    }

    fun getAllHistoryOfLast3Weeks(): List<History> {
        return historyService.getAll().filter { it.dateCroissant > Date.from(Instant.now().minus(21, ChronoUnit.DAYS)) }
    }
}