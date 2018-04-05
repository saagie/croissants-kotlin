package io.saagie.croissants.slack

import me.ramswaroop.jbot.core.slack.Bot
import me.ramswaroop.jbot.core.slack.Controller
import me.ramswaroop.jbot.core.slack.EventType
import me.ramswaroop.jbot.core.slack.models.Event
import me.ramswaroop.jbot.core.slack.models.Message
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession

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
                    |To have all available commands, type : /ap-command
                """.trimMargin()))
    }







}