package io.saagie.croissants.service

import io.saagie.croissants.domain.History
import io.saagie.croissants.domain.User
import io.saagie.croissants.slack.SlackBot
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*

@RestController
class DrawService(val userService: UserService,
                  val slackBot: SlackBot,
                  val historyService: HistoryService,
                  val utilService: UtilService) {

    @Scheduled(cron = "0 0 8 * * WED")
    fun scheduleSelection() {
        val nextFriday = utilService.getNextFriday()
        if (historyService.getByDate(utilService.localDateToDate(nextFriday)) == null) {
            val userDraw = this.drawUser()
            historyService.save(History(emailUser = userDraw.email, dateCroissant = utilService.localDateToDate(nextFriday)))
            slackBot.sendDM(userDraw,announcementMessage(nextFriday))
            slackBot.sendDM(userService.getByEmail("kevin@saagie.com"),announcementMessage(nextFriday,userDraw))
        }
    }

    @Scheduled(cron = "0 0 1 * * *")
    fun updateCoef() {
        val history = historyService.getByDate(utilService.localDateToDate(LocalDate.now()))
        if (history != null) {
            var user = userService.getByEmail(history.emailUser as String)
            user.coefficient=1
            userService.save(user)
        }
    }

    fun drawUser(): User{
        val userList = userService.findUsersToDraw()
        var coef: Int
        var drawList: MutableList<User> = mutableListOf()
        var rand: Int

        userList.forEach {
            coef = userService.getWeightedCoefficient(it)
            for (i in 1..coef){
                drawList.add(it)
            }
        }

        drawList.shuffle()

        rand = Random().nextInt(drawList.size)

        return drawList[rand]
    }

    fun announcementMessage(date: LocalDate): String {
        var message = "You have been drawn to bring the croissants on ${date}"

        return message
    }

    fun announcementMessage(date: LocalDate, user: User): String {
        var message = "${user.username} have been drawn to bring the croissants on ${date}"

        return message
    }
}