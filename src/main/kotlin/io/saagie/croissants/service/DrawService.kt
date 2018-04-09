package io.saagie.croissants.service

import io.saagie.croissants.domain.User
import io.saagie.croissants.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class DrawService(val userService: UserService) {

    @GetMapping("/test_draw")
    fun drawUser(): User{
        val userList = userService.findUsersToDraw()
        var coef: Int
        var drawList: MutableList<User> = mutableListOf()
        var rand: Int

        userList.forEach {
            coef = userService.getWeightedCoefficient(it)
            println("coef : ${coef}")
            for (i in 1..coef){
                drawList.add(it)
            }
        }

        drawList.shuffle()

        rand = Random().nextInt(drawList.size)

        return drawList[rand]
    }



}