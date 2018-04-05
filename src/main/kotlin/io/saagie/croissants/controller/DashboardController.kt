package io.saagie.croissants.controller

import io.saagie.croissants.dao.RequestDao
import io.saagie.croissants.domain.Proposition
import io.saagie.croissants.domain.Request
import io.saagie.croissants.domain.Schedule
import io.saagie.croissants.domain.User
import io.saagie.croissants.service.DrawService
import io.saagie.croissants.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DashboardController(val userService: UserService, val drawService: DrawService, val requestDao: RequestDao) {

    @GetMapping("/dashboard")
    fun dashboard(): Dashboard {
        return Dashboard(
                users = userService.getAll().sortedBy { it.attribution },
                schedule = drawService.getCurrentSchedules(),
                proposition = drawService.getAllPropositions()?.groupBy { it.spotNumber },
                requests = requestDao.findAll()
        )
    }


    data class Dashboard(
            val users: List<User>,
            val schedule: List<Schedule>,
            val proposition: Map<Int, List<Proposition>>?,
            val requests: List<Request>?

    )
}