package io.saagie.croissants.controller

import io.saagie.croissants.domain.User
import io.saagie.croissants.domain.History
import io.saagie.croissants.service.UserService
import io.saagie.croissants.service.HistoryService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(val userService: UserService,val historyService: HistoryService) {

    @GetMapping("/dashboard")
    fun dashboard(): Dashboard {
        return Dashboard(
                users = userService.getAll().sortedBy { it.coefficient },
                historys = historyService.getAll()
        )
    }


    data class Dashboard(
            val users: List<User>,
            val historys: List<History>
    )
}