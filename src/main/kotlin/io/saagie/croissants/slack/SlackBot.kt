package io.saagie.croissants.slack


import io.saagie.croissants.domain.User
import me.ramswaroop.jbot.core.slack.Bot
import me.ramswaroop.jbot.core.slack.Controller
import me.ramswaroop.jbot.core.slack.EventType
import me.ramswaroop.jbot.core.slack.models.Event
import me.ramswaroop.jbot.core.slack.models.Message
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.WebSocketSession
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


@Component
class SlackBot : Bot() {

    @Value("\${slackBotToken}")
    private val slackToken: String? = null

    @Value("\${slackWebhookUrl}")
    private val slackWebhookUrl: String = ""

    override fun getSlackToken(): String? {
        return slackToken
    }

    override fun getSlackBot(): Bot {
        return this
    }

    val logger = LoggerFactory.getLogger("SlackBot")


    @Controller(events = arrayOf(EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE))
    fun onReceiveDM(session: WebSocketSession, event: Event) {
        reply(session, event, Message(
                """Hi, I am ${slackService.currentUser.name}
                    |You can interact with me using commands.
                    |To have all available commands, type : /c-command
                """.trimMargin()))
    }

    fun selection(user: User,date: LocalDate) {

        val message = Message("*******************\n")
        message.text += ":game_die: Draw is done for the ${date.format(DateTimeFormatter.ofPattern("dd/MM"))} the person selected is :  ${ user.username } \n"
        message.text += "*******************"
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

    fun purpose(user: User,date: LocalDate) {
        val message = Message("Croissant has been purpose the ${date.format(DateTimeFormatter.ofPattern("dd/MM"))} by ${ user.username } \n Thanks.\n")
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }





}