package io.saagie.croissants.slack

import io.saagie.croissants.service.UserService
import me.ramswaroop.jbot.core.slack.models.Attachment
import me.ramswaroop.jbot.core.slack.models.Message
import me.ramswaroop.jbot.core.slack.models.RichMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class SlackSlashCommand(
        val userService: UserService,
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
                    setText("/ap-command : this list of all available commands")
                },
                Attachment().apply {
                    setText("/ap-register : to register you as a new member of Aston Parking")
                },
                Attachment().apply {
                    setText("/ap-unregister : to unregister your account")
                },
                Attachment().apply {
                    setText("/ap-profile : to display your AstonParking profile")
                },
                Attachment().apply {
                    setText("/ap-attribution : to display attribution for the current and the next week")
                },
                Attachment().apply {
                    setText("/ap-today : to see today spots attribution")
                },
                Attachment().apply {
                    setText("/ap-pick-today : to pick a spot today 'if available' = /ap-pick TODAY")
                },
                Attachment().apply {
                    setText("/ap-inactive-profile : to disable your AstonParking profile")
                },
                Attachment().apply {
                    setText("/ap-active-profile : to enable your AstonParking profile")
                },
                Attachment().apply {
                    setText("/ap-accept : to accept all attribution")
                },
                Attachment().apply {
                    setText("/ap-decline : to decline all attribution")
                },
                Attachment().apply {
                    setText("/ap-release dd/MM : to release an accepted spot for the specified date (day/month)")
                },
                Attachment().apply {
                    setText("/ap-pick dd/MM : to pick a free spot for the specified date (day/month)")
                },
                Attachment().apply {
                    setText("/ap-planning : to display personnal planning for the current and the next week")
                },
                Attachment().apply {
                    setText("/ap-request dd/MM : to make a request for a spot on a desired date (only one per user and double debit when selected)")
                },
                Attachment().apply {
                    setText("/ap-request-cancel : to cancel your current request")
                }
        )
        richMessage.attachments = attachments


        return richMessage.encodedMessage()
    }

    @RequestMapping(value = ["/slack/register"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveRegisterCommand(@RequestParam("token") token: String,
                                 @RequestParam("team_id") teamId: String,
                                 @RequestParam("team_domain") teamDomain: String,
                                 @RequestParam("channel_id") channelId: String,
                                 @RequestParam("channel_name") channelName: String,
                                 @RequestParam("user_id") userId: String,
                                 @RequestParam("user_name") userName: String,
                                 @RequestParam("command") command: String,
                                 @RequestParam("text") text: String,
                                 @RequestParam("response_url") responseUrl: String): RichMessage {
        val registerUser = userService.registerUser(userName, userId)
        val richMessage = RichMessage("Register : ${userName}")
        val attachments = arrayOfNulls<Attachment>(1)
        attachments[0] = Attachment()
        attachments[0]!!.setText("Welcome on Aston Parking ${userName}. Please log in on the website : ${url} to complete your registration.")
        if (!registerUser) {
            attachments[0]!!.setText("You are already registred on AstonParking. Use /ap-profile to see your profile.")
        }
        richMessage.attachments = attachments


        return richMessage.encodedMessage()
    }

    @RequestMapping(value = ["/slack/unregister"],
            method = [(RequestMethod.POST)],
            consumes = [(MediaType.APPLICATION_FORM_URLENCODED_VALUE)])
    fun onReceiveUnregisterCommand(@RequestParam("token") token: String,
                                 @RequestParam("team_id") teamId: String,
                                 @RequestParam("team_domain") teamDomain: String,
                                 @RequestParam("channel_id") channelId: String,
                                 @RequestParam("channel_name") channelName: String,
                                 @RequestParam("user_id") userId: String,
                                 @RequestParam("user_name") userName: String,
                                 @RequestParam("command") command: String,
                                 @RequestParam("text") text: String,
                                 @RequestParam("response_url") responseUrl: String): Message {
        val unregisterUser = userService.unregisterUser(userId)

        var message = Message("Next monday, your account will be remove. If you want to cancel this request, type /ap-unregister again.")
        if (!unregisterUser){
            message = Message("Your account will NOT be remove.")
        }
        return message
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
            val richMessage = RichMessage("Profile : ${user.username}")
            if (!user.activated) {
                richMessage.text = richMessage.text + """
                    - :warning: *PROFILE NOT ACTIVATED* :warning: -
                    Please log in AstonParking to activate your profile (${url})
                    """.trimIndent()
            }
            var requestMessage = "None."


            var chance="<10%"

            val attachments = arrayOf(
                    Attachment().apply {
                        setText("Username : ${user.username}")
                    },
                    Attachment().apply {
                        setText("Email : ${user.email}")
                    },
                    Attachment().apply {
                        setText("Attribution : ${user.attribution}")
                    },
                    Attachment().apply {
                        setText("Status :  ${user.status()}")
                    },
                    Attachment().apply {
                        setText("Pending request :  ${requestMessage}")
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



}