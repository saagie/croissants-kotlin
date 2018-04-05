package io.saagie.astonparking.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal


@RestController
class SlackOAuthController {

    @GetMapping("/user")
    fun user(principal: Principal): Principal {
        return principal
    }

}