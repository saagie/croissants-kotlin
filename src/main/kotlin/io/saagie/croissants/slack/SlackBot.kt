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
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpEntity
import org.apache.catalina.manager.StatusTransformer.setContentType
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.Charset


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
            val result = restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

    fun propose(user: User,date: LocalDate) {
        val message = Message("Croissant has been propose the ${date.format(DateTimeFormatter.ofPattern("dd/MM"))} by ${ user.username } \n Thanks.\n")
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

    fun sendDM(user: User,message: String) {

        val restTemplate = RestTemplate()
        try {
            val requestJson = "{\"token\":\"$slackToken\",\"channel\":\"@${user.id}\",\"text\":\"$message\",\"username\":\"Croissants\"}"
            val url = "https://slack.com/api/chat.postMessage"
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.acceptCharset = arrayListOf(Charsets.UTF_8)
            headers.set("Authorization","Bearer $slackToken")

            val entity = HttpEntity<String>(requestJson, headers)
            restTemplate.postForEntity(url, entity, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

}
