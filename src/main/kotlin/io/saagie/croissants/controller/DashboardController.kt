package io.saagie.croissants.controller

import io.saagie.croissants.domain.User
import io.saagie.croissants.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(val userService: UserService) {

    @GetMapping("/dashboard")
    fun dashboard(): Dashboard {
        return Dashboard(
                users = userService.getAll().sortedBy { it.coefficient }
        )
    }


    data class Dashboard(
            val users: List<User>
    )
}