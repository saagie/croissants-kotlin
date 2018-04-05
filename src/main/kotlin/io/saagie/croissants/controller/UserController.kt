package io.saagie.croissants.controller

import io.saagie.croissants.domain.User
import io.saagie.croissants.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(val userService: UserService)  {

    @GetMapping("/profile")
    fun profile(principal: OAuth2Authentication): User {
        return userService.get("1")

    }

    @PatchMapping("/profile/status")
    @ResponseStatus(HttpStatus.OK)
    fun changeStatus(principal: OAuth2Authentication) {
        userService.changeStatus("1")
    }


}