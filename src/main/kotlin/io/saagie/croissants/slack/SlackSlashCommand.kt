package io.saagie.croissants.slack

import io.saagie.croissants.service.UserService
import io.saagie.croissants.service.HistoryService
import io.saagie.croissants.service.DrawService
import io.saagie.croissants.service.UtilService
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.Message
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


@RestController
class SlackSlashCommand(
        val userService: UserService,
        val historyService: HistoryService,
        val drawService: DrawService,
        val utilService: UtilService
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
                    setText("/c-trap : to trap a unlock workstation")
                },
                Attachment().apply {
                    setText("/c-top : to see all coefficient")
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
            val draw = historyService.getAllByEmailUser(user.email!!).size
            val totalCoef = userService.getAllActive().sumBy { userService.getWeightedCoefficient(it) }
            val chance:Double= ((weightedCoefficient / totalCoef.toDouble())*100)
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
                        setText("Chance of being selected :  ${ BigDecimal(chance).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble() }%")
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
    fun onReceiveSelectedCommand(@RequestParam("token") token: String,
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
            val user = userService.getByEmail(history!!.emailUser!!)
            val date= history.dateCroissant
            message.text += "Next Selected is : *${ user.username}* for ${ date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/YY")) }  \n\n"
            message.text += "*******************\n"

        }else{

            message.text += "No next selected person found\n"
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
        if (! drawService.acceptSelection(userId)) {
            message.text = "You have no selection or have already accept or decline for the next friday."
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



        val message = Message("You've declined your selection")
        if (! drawService.declineSelection(userId)) {
            message.text = "You have no selection or have already accept or decline for the next friday."
        }
        return message
    }

    @RequestMapping(value = ["/slack/purpose"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceivePurposeCommand(@RequestParam("token") token: String,
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
            val result = historyService.purpose(userId, utilService.extractDate(text))
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
    fun onReceivePurposeNextCommand(@RequestParam("token") token: String,
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
            val result = historyService.purpose(userId, utilService.getNextFriday())
            return if (result){
                Message("You have purpose the croissant for the day (${text}) .")
            }else{
                Message("Someone already purpose the croissant for the day (${text}) .")
            }
        } catch (iae: IllegalArgumentException) {
            return Message(iae.message)
        }

    }


    @RequestMapping(value = ["/slack/top-ten"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveToptenCommand(@RequestParam("token") token: String,
                              @RequestParam("team_id") teamId: String,
                              @RequestParam("team_domain") teamDomain: String,
                              @RequestParam("channel_id") channelId: String,
                              @RequestParam("channel_name") channelName: String,
                              @RequestParam("user_id") userId: String,
                              @RequestParam("user_name") userName: String,
                              @RequestParam("command") command: String,
                              @RequestParam("text") text: String,
                              @RequestParam("response_url") responseUrl: String): Message {


        var users = userService.getAll().sortedByDescending { userService.getWeightedCoefficient(it) }

        val message = Message("*******************\n")
        message.text += "*Top Ten*\n\n"
        val nkeep = 10
        var i = nkeep
        users = users.take(nkeep)
        users.map({  message.text += "${ nkeep+1 - i-- }. ${ it.username } : ${ userService.getWeightedCoefficient(it)  } \n"})


        message.text += "*******************\n"


        return message
    }



    @RequestMapping(value = ["/slack/top"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveTopCommand(@RequestParam("token") token: String,
                               @RequestParam("team_id") teamId: String,
                               @RequestParam("team_domain") teamDomain: String,
                               @RequestParam("channel_id") channelId: String,
                               @RequestParam("channel_name") channelName: String,
                               @RequestParam("user_id") userId: String,
                               @RequestParam("user_name") userName: String,
                               @RequestParam("command") command: String,
                               @RequestParam("text") text: String,
                               @RequestParam("response_url") responseUrl: String): Message {


        var users = userService.getAll().sortedByDescending { userService.getWeightedCoefficient(it) }

        val message = Message("*******************\n")
        message.text += "*Top*\n\n"
        val lsize= users.size
        var i = lsize
        users.map({  message.text += "${ lsize+1 - i-- }. ${ it.username } : ${ userService.getWeightedCoefficient(it)  } \n"})
        message.text += "*******************\n"


        return message
    }

    @RequestMapping(value = ["/slack/random"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveRandomCommand(@RequestParam("token") token: String,
                                 @RequestParam("team_id") teamId: String,
                                 @RequestParam("team_domain") teamDomain: String,
                                 @RequestParam("channel_id") channelId: String,
                                 @RequestParam("channel_name") channelName: String,
                                 @RequestParam("user_id") userId: String,
                                 @RequestParam("user_name") userName: String,
                                 @RequestParam("command") command: String,
                                 @RequestParam("text") text: String,
                                 @RequestParam("response_url") responseUrl: String): Message {


        val fact = arrayOf(
                "One of the developer lost lot of time because he forgot a 's' in url and doesn't understand why he have a 404*",
                "90% of this app be develop in underwear, as our CTO learn to us*",
                "30% of source code has been taken from aston-parking develop by Pierre Leresteux*",
                "One of the developer ask for support to Pierre Leresteux everyday (Sunday include) and Pierre always answer*",
                "Lot of prince were ~killed~ eaten during the development of this app*",
                "5% of code come from StackOverflow*",
                "As this app doesn't be tested by Sandrine, we expect many bugs*"
        )

        return  Message("* ${ fact[Random().nextInt(fact.size)] }\n\n")
    }
    @RequestMapping(value = ["/slack/trap"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveTrapCommand(@RequestParam("token") token: String,
                               @RequestParam("team_id") teamId: String,
                               @RequestParam("team_domain") teamDomain: String,
                               @RequestParam("channel_id") channelId: String,
                               @RequestParam("channel_name") channelName: String,
                               @RequestParam("user_id") userId: String,
                               @RequestParam("user_name") userName: String,
                               @RequestParam("command") command: String,
                               @RequestParam("text") text: String,
                               @RequestParam("response_url") responseUrl: String): Message {

        userService.save(userService.get(userId).incrementCoefficient(10))

        val message = Message("*******************\n")
        message.text += ":flic: *Un default de sécurisation à été répéré* \n\n"
        message.text += "+10 point au croissant !* \n\n"
        message.text += "*******************\n"
        return message
    }
}
