package io.saagie.astonparking.controller

import org.springframework.security.oauth2.provider.OAuth2Authentication

interface SecurityController {

    open fun extractUserId(principal: OAuth2Authentication): String {
        val map = principal.userAuthentication.principal as Map<*, *>
        val id = map.get("id") as String
        return id
    }
}