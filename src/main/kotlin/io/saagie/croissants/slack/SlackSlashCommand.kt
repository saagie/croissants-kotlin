package io.saagie.croissants.slack

import io.saagie.croissants.domain.User
import io.saagie.croissants.service.UserService
import io.saagie.croissants.service.DrawService
import io.saagie.croissants.service.HistoryService
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.Message
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters




@RestController
class SlackSlashCommand(
        val userService: UserService,
        val historyService: HistoryService,
        val drawService: DrawService,
        val slackBot: SlackBot
) {

    @Value("\${url}")
    private val url: String? = null

    @RequestMapping(value = ["/slack/command"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveHelpCommand(@RequestParam("token") token: String,
                             @RequestParam("team_id") teamId: String,
                             @RequestParam("team_domain") teamDomain: String,
                             @RequestParam("channel_id") channelId: String,
                             @RequestParam("channel_name") channelName: String,
                             @RequestParam("user_id") userId: String,
                             @RequestParam("user_name") userName: String,
                             @RequestParam("command") command: String,
                             @RequestParam("text") text: String,
                             @RequestParam("response_url") responseUrl: String): RichMessage {
        val richMessage = RichMessage("---Aston Parking : Available commands--- ")
        val attachments = arrayOf(
                Attachment().apply {
                    setText("/c-command : this list of all available commands")
                },
                Attachment().apply {
                    setText("/c-profile : to display your Croissants profile")
                },
                Attachment().apply {
                    setText("/c-selected : to display selected for the next friday")
                },
                Attachment().apply {
                    setText("/c-top-ten : to see top ten highest coefficient")
                },
                Attachment().apply {
                    setText("/c-propose-next : to purpose the croissant for next friday")
                },
                Attachment().apply {
                    setText("/c-inactive-profile : to disable your Croissants profile during holiday")
                },
                Attachment().apply {
                    setText("/c-active-profile : to enable your Croissants profile after holiday")
                },
                Attachment().apply {
                    setText("/c-accept : to accept selection")
                },
                Attachment().apply {
                    setText("/c-decline : to decline selection (+10 coefficient)")
                },
                Attachment().apply {
                    setText("/c-propose dd/MM : to purpose the croissant  for the specified date (day/month)")
                },
                Attachment().apply {
                    setText("/c-planning : to display personnal planning for the current and the next week")
                }
        )
        richMessage.attachments = attachments


        return richMessage.encodedMessage()
    }


    @RequestMapping(value = ["/slack/profile/active"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveActiveProfileCommand(@RequestParam("token") token: String,
                                      @RequestParam("team_id") teamId: String,
                                      @RequestParam("team_domain") teamDomain: String,
                                      @RequestParam("channel_id") channelId: String,
                                      @RequestParam("channel_name") channelName: String,
                                      @RequestParam("user_id") userId: String,
                                      @RequestParam("user_name") userName: String,
                                      @RequestParam("command") command: String,
                                      @RequestParam("text") text: String,
                                      @RequestParam("response_url") responseUrl: String): RichMessage {
        userService.changeStatus(userId, true)
        val richMessage = RichMessage("Activate Profile : ${userName}")
        val attachments = arrayOfNulls<Attachment>(1)
        attachments[0] = Attachment()
        attachments[0]!!.setText("Your profile is now active.")
        richMessage.attachments = attachments

        return richMessage.encodedMessage()
    }

    @RequestMapping(value = ["/slack/profile/inactive"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveInactiveProfileCommand(@RequestParam("token") token: String,
                                        @RequestParam("team_id") teamId: String,
                                        @RequestParam("team_domain") teamDomain: String,
                                        @RequestParam("channel_id") channelId: String,
                                        @RequestParam("channel_name") channelName: String,
                                        @RequestParam("user_id") userId: String,
                                        @RequestParam("user_name") userName: String,
                                        @RequestParam("command") command: String,
                                        @RequestParam("text") text: String,
                                        @RequestParam("response_url") responseUrl: String): RichMessage {
        userService.changeStatus(userId, false)
        val richMessage = RichMessage("Desactivate Profile : ${userName}")
        val attachments = arrayOfNulls<Attachment>(1)
        attachments[0] = Attachment()
        attachments[0]!!.setText("Your profile is now inactive.")
        richMessage.attachments = attachments

        return richMessage.encodedMessage()
    }

    @RequestMapping(value = ["/slack/profile"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveProfileCommand(@RequestParam("token") token: String,
                                @RequestParam("team_id") teamId: String,
                                @RequestParam("team_domain") teamDomain: String,
                                @RequestParam("channel_id") channelId: String,
                                @RequestParam("channel_name") channelName: String,
                                @RequestParam("user_id") userId: String,
                                @RequestParam("user_name") userName: String,
                                @RequestParam("command") command: String,
                                @RequestParam("text") text: String,
                                @RequestParam("response_url") responseUrl: String): RichMessage {


        try {
            val user = userService.get(userId)
            val weightedCoefficient = userService.getWeightedCoefficient(user)
            val draw = historyService.getAllByUser(user.id)
            val chance = drawService.getChance(user)
            val richMessage = RichMessage("Profile : ${user.username}")

            val attachments = arrayOf(
                    Attachment().apply {
                        setText("Username : ${user.username}")
                    },
                    Attachment().apply {
                        setText("Email : ${user.email}")
                    },
                    Attachment().apply {
                        setText("Coefficient : ${user.coefficient}")
                    },
                    Attachment().apply {
                        setText("Draw count : ${draw}")
                    },
                    Attachment().apply {
                        setText("Weighted coefficient : ${weightedCoefficient}")
                    },
                    Attachment().apply {
                        setText("Status :  ${user.status()}")
                    },
                    Attachment().apply {
                        setText("Chance of being selected :  ${chance}")
                    }
            )
            richMessage.attachments = attachments

            return richMessage.encodedMessage()
        } catch (e: Exception) {
            val richMessage = RichMessage("The profile : ${userName} doesn't exist. Type /ap-register to create your profile.")
            return richMessage.encodedMessage()
        }

    }

    @RequestMapping(value = ["/slack/selected"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveAttributionCommand(@RequestParam("token") token: String,
                                    @RequestParam("team_id") teamId: String,
                                    @RequestParam("team_domain") teamDomain: String,
                                    @RequestParam("channel_id") channelId: String,
                                    @RequestParam("channel_name") channelName: String,
                                    @RequestParam("user_id") userId: String,
                                    @RequestParam("user_name") userName: String,
                                    @RequestParam("command") command: String,
                                    @RequestParam("text") text: String,
                                    @RequestParam("response_url") responseUrl: String): Message {

        val history = historyService.getLastSelected()

        var message = Message("*******************\n")
        if (history != null) {
            val user = userService.get(history!!.idUser!!)
            val date= history.dateCroissant
            message.text += "Last selected person"
            message.text += "*******************\n"
            message.text += "*${ user.username} for ${date.date } \n\n"
            message.text += "*******************\n"

        }else{

            message.text += "No last selected person found"
            message.text += "*******************\n"
        }


        return message
    }


    @RequestMapping(value = ["/slack/accept"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveAcceptCommand(@RequestParam("token") token: String,
                               @RequestParam("team_id") teamId: String,
                               @RequestParam("team_domain") teamDomain: String,
                               @RequestParam("channel_id") channelId: String,
                               @RequestParam("channel_name") channelName: String,
                               @RequestParam("user_id") userId: String,
                               @RequestParam("user_name") userName: String,
                               @RequestParam("command") command: String,
                               @RequestParam("text") text: String,
                               @RequestParam("response_url") responseUrl: String): Message {


        val message = Message("OK, your selection are accepted.")
        if (!drawService.acceptProposition(userId)) {
            message.text = "You have no selection for the next friday."
        }

        return message
    }

    @RequestMapping(value = ["/slack/decline"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveDeclineCommand(@RequestParam("token") token: String,
                                @RequestParam("team_id") teamId: String,
                                @RequestParam("team_domain") teamDomain: String,
                                @RequestParam("channel_id") channelId: String,
                                @RequestParam("channel_name") channelName: String,
                                @RequestParam("user_id") userId: String,
                                @RequestParam("user_name") userName: String,
                                @RequestParam("command") command: String,
                                @RequestParam("text") text: String,
                                @RequestParam("response_url") responseUrl: String): Message {


        drawService.declineProposition(userId)

        val message = Message("You've declined your selection")
        return message
    }

    @RequestMapping(value = ["/slack/purpose"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceivePickCommand(@RequestParam("token") token: String,
                             @RequestParam("team_id") teamId: String,
                             @RequestParam("team_domain") teamDomain: String,
                             @RequestParam("channel_id") channelId: String,
                             @RequestParam("channel_name") channelName: String,
                             @RequestParam("user_id") userId: String,
                             @RequestParam("user_name") userName: String,
                             @RequestParam("command") command: String,
                             @RequestParam("text") text: String,
                             @RequestParam("response_url") responseUrl: String): Message {

        try {
            val result = drawService.purpose(userId, text)
            val message = if (result){
                Message("You have purpose the croissant for the day (${text}) .")
            }else{
                Message("Someone already purpose the croissant for the day (${text}) .")
            }
            return message
        } catch (iae: IllegalArgumentException) {
            return Message(iae.message)
        }

    }

    @RequestMapping(value = ["/slack/purpose-next"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceivePickTodayCommand(@RequestParam("token") token: String,
                             @RequestParam("team_id") teamId: String,
                             @RequestParam("team_domain") teamDomain: String,
                             @RequestParam("channel_id") channelId: String,
                             @RequestParam("channel_name") channelName: String,
                             @RequestParam("user_id") userId: String,
                             @RequestParam("user_name") userName: String,
                             @RequestParam("command") command: String,
                             @RequestParam("text") text: String,
                             @RequestParam("response_url") responseUrl: String): Message {

        try {
            val input = LocalDate.now()
            val nextFriday = input.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
            val result = drawService.purpose(userId, nextFriday)
            val message = if (result){
                Message("You have purpose the croissant for the day (${text}) .")
            }else{
                Message("Someone already purpose the croissant for the day (${text}) .")
            }
            return message
        } catch (iae: IllegalArgumentException) {
            return Message(iae.message)
        }

    }


    @RequestMapping(value = ["/slack/top-ten"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceivePlanningCommand(@RequestParam("token") token: String,
                              @RequestParam("team_id") teamId: String,
                              @RequestParam("team_domain") teamDomain: String,
                              @RequestParam("channel_id") channelId: String,
                              @RequestParam("channel_name") channelName: String,
                              @RequestParam("user_id") userId: String,
                              @RequestParam("user_name") userName: String,
                              @RequestParam("command") command: String,
                              @RequestParam("text") text: String,
                              @RequestParam("response_url") responseUrl: String): Message {


        val users = userService.getAll()
        users.sortedBy { userService.getWeightedCoefficient(it) }

        val message = Message("*******************\n")
        message.text += "*Top Ten\n\n"
        var i = 10
        var current:User
       while (i>=0){

          current = users.first()

           message.text += "${ 10-i }. ${ current.username } : ${ userService.getWeightedCoefficient(current) } \n"
       }

        message.text += "*******************\n"


        return message
    }


}