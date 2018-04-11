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
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
        val richMessage = RichMessage("---Croissants : Available commands--- ")
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
                    setText("/c-selected-next : to display all next selected")
                },
                Attachment().apply {
                    setText("/c-trap email : to trap a unlock workstation")
                },
                Attachment().apply {
                    setText("/c-top : to see all coefficient")
                },
                Attachment().apply {
                    setText("/c-top-ten : to see top ten highest coefficient")
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
                    setText("/c-propose-next : to purpose the croissant for next friday")
                },
                Attachment().apply {
                    setText("/c-remove-propose : to remove purpose the croissant")
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
            val chance :Double  = if (totalCoef>0)
            {

                ((weightedCoefficient / totalCoef.toDouble()) * 100)
            }else
            {
                0.0

            }
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
                        setText("Chance of being selected :  ${ if (user.enable) { BigDecimal(chance).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble() } else { "0"}}%")
                    }
            )
            richMessage.attachments = attachments

            return richMessage.encodedMessage()
        } catch (e: Exception) {
            val richMessage = RichMessage("The profile : ${userName} doesn't exist. Go to $url to create your account.")
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
            val date = history.dateCroissant
            message.text += "Next Selected is : *${user.username}* for ${date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/YY"))}  \n\n"
            message.text += "*******************\n"

        } else {

            message.text += "No next selected person found\n"
            message.text += "*******************\n"
        }


        return message
    }

    @RequestMapping(value = ["/slack/selected-next"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveSelectedNextCommand(@RequestParam("token") token: String,
                                     @RequestParam("team_id") teamId: String,
                                     @RequestParam("team_domain") teamDomain: String,
                                     @RequestParam("channel_id") channelId: String,
                                     @RequestParam("channel_name") channelName: String,
                                     @RequestParam("user_id") userId: String,
                                     @RequestParam("user_name") userName: String,
                                     @RequestParam("command") command: String,
                                     @RequestParam("text") text: String,
                                     @RequestParam("response_url") responseUrl: String): Message {

        val history = historyService.getNextSelected()

        var message = Message("*******************\n")

        if (history.isEmpty()) {
            message.text += "No next selected person found\n"
            message.text += "*******************\n"
        } else {
            message.text += "Next Selected is : \n"
            history.forEach {
                val user = userService.getByEmail(it!!.emailUser!!)
                val date = it.dateCroissant
                message.text += "- *${user.username}* for ${date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/YY"))}  \n\n"
            }
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
        if (!drawService.acceptSelection(userId)) {
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
        if (!drawService.declineSelection(userId)) {
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
            return if (result) {
                Message("You have purpose the croissant for the day (${text}) .")
            } else {
                val history = historyService.getByDate(utilService.localDateToDate(utilService.extractDate(text))).filter { it.ok != 2 }.firstOrNull()
                if (history != null) {
                    val user = userService.getByEmail(history!!.emailUser!!)
                    Message("${user.username} already purpose the croissant for the day (${text}) .")
                } else {
                    Message("Somebody already purpose the croissant for the day (${text}) .")
                }
            }
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
            val date = utilService.getNextFriday()
            val result = historyService.purpose(userId, date)
            return if (result) {
                Message("You have purpose the croissant for the day (${date.format(DateTimeFormatter.ofPattern("dd/MM/YY"))}) .")
            } else {
                val history = historyService.getByDate(utilService.localDateToDate(date)).filter { it.ok != 2 }.firstOrNull()
                if (history != null) {
                    val user = userService.getByEmail(history!!.emailUser!!)
                    Message("${user.username} already purpose the croissant for the day (${date.format(DateTimeFormatter.ofPattern("dd/MM/YY"))})  .")
                } else {
                    Message("Somebody already purpose the croissant for the day (${date.format(DateTimeFormatter.ofPattern("dd/MM/YY"))})  .")
                }
            }
        } catch (iae: IllegalArgumentException) {
            return Message(iae.message)
        }

    }

    @RequestMapping(value = ["/slack/remove-purpose"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveRemovePurposeCommand(@RequestParam("token") token: String,
                                @RequestParam("team_id") teamId: String,
                                @RequestParam("team_domain") teamDomain: String,
                                @RequestParam("channel_id") channelId: String,
                                @RequestParam("channel_name") channelName: String,
                                @RequestParam("user_id") userId: String,
                                @RequestParam("user_name") userName: String,
                                @RequestParam("command") command: String,
                                @RequestParam("text") text: String,
                                @RequestParam("response_url") responseUrl: String): Message {



        val date = utilService.extractDate(text)
        return  if ( Date.from(Instant.now()) > utilService.localDateToDate(date.minus(2, ChronoUnit.DAYS))   )
        {
              Message("It's too late for remove proposition. Try to find someone for replace you.")

        }else {
            val history = historyService.getByDate(utilService.localDateToDate(date)).filter { it.ok != 2 }.firstOrNull()
             if (history != null) {
                historyService.delete(history)
                Message("Your purpose has been removed (${text}).")


            } else {
                Message("You don't have purpose this day (${text}).")

            }
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
        users.map({
            message.text += "${nkeep + 1 - i--}. ${it.username} : ${userService.getWeightedCoefficient(it)}  ${if (!it.enable) {
                "_inactive_"
            } else {
                ""
            }} \n"
        })


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
        val lsize = users.size
        var i = lsize
        val allCoefficient = users.sumBy { userService.getWeightedCoefficient(it) }
        users.map({
            message.text += "${lsize + 1 - i--}. ${it.username} : ${userService.getWeightedCoefficient(it)}  ${BigDecimal(((userService.getWeightedCoefficient(it)  / allCoefficient.toDouble()) * 100)).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()   }%   ${if (!it.enable) {
                "_inactive_"
            } else {
                ""
            }}\n"
        })
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

        return Message("* ${fact[Random().nextInt(fact.size)]}\n\n")
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

        var user = userService.get(userId)
        var trapAuthor = ""
        if (text != "")
        {
           var trap = userService.getByEmail(text)
            trap.incrementCoefficient(-5)
            userService.save(trap)
            trapAuthor = "You have been trapped by " + trap.username +"\n\n"
        }


        return if (user.lastUp == null || Duration.between(
                        user.lastUp!!.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        Date.from(Instant.now()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                ).toMinutes() >= 20) {

            user.incrementCoefficient(10)
            user.lastUp = Date.from(Instant.now())
            userService.save(user)
            Message( "*******************\n"+
             " *A default of security has been detected* \n\n"+
             "Your coefficient increases by 10 points !\n\n"+
                    trapAuthor +
             "*******************\n")

        }else{
            Message("*******************\n"+
                    " *This person has already been trapped in the last 20 minutes* \n\n"+
                    "*******************\n")
        }


    }
}
