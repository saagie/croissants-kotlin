package io.saagie.astonparking.controller

import io.saagie.astonparking.service.DrawService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class DrawController(val drawService: DrawService) : SecurityController {

    @PostMapping("/draw")
    @ResponseStatus(HttpStatus.OK)
    fun makeADraw() {
        drawService.attribution(null)
    }

    @PostMapping("/accept")
    @ResponseStatus(HttpStatus.OK)
    fun acceptAttribution(principal: OAuth2Authentication) {
        val id = extractUserId(principal)
        drawService.acceptProposition(id)
    }

    @PostMapping("/decline")
    @ResponseStatus(HttpStatus.OK)
    fun declineAttribution(principal: OAuth2Authentication) {
        val id = extractUserId(principal)
        drawService.declineProposition(id)
    }

}