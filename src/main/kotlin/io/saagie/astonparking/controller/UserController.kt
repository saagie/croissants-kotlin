package io.saagie.astonparking.controller

import io.saagie.astonparking.domain.User
import io.saagie.astonparking.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(val userService: UserService) : SecurityController {

    @GetMapping("/profile")
    fun profile(principal: OAuth2Authentication): User {
        val id = extractUserId(principal)
        return userService.get(id)

    }

    @PatchMapping("/profile/status")
    @ResponseStatus(HttpStatus.OK)
    fun changeStatus(principal: OAuth2Authentication) {
        val id = extractUserId(principal)
        userService.changeStatus(id)
    }


}