package io.saagie.astonparking.slack


import io.saagie.astonparking.domain.Proposition
import io.saagie.astonparking.domain.Schedule
import io.saagie.astonparking.domain.User
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
                    |To have all available commands, type : /ap-command
                """.trimMargin()))
    }

    fun proposition(propositions: ArrayList<Proposition>, sortedActiveUsers: List<User>, nextMonday: LocalDate) {

        val message = Message("*******************\n")
        message.text += ":game_die: Draw is done for the week started the ${nextMonday.format(DateTimeFormatter.ofPattern("dd/MM"))} \n"
        message.text += generateTextForPropositions(propositions, sortedActiveUsers, null)
        message.text += "\nYou can see attributions for the current and the next week by using the command /ap-attribution\n"
        message.text += "*******************"
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

    fun spotRelease(date: LocalDate) {
        val message = Message("A spot has been released the ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}. You can pick it with the command /ap-pick ${date.format(DateTimeFormatter.ofPattern("dd/MM"))} \n")
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

    fun generateTextForPropositions(propositions: ArrayList<Proposition>, sortedActiveUsers: List<User>, userId: String?): String {
        var message = ""
        val mapText = hashMapOf<Int, ArrayList<Proposition>>()
        propositions.forEach { proposition ->
            run {
                if (userId == null || userId == proposition.userId) {
                    val listForSpotNumber = mapText.getOrDefault(proposition.spotNumber, arrayListOf())
                    listForSpotNumber.add(proposition)
                    mapText.put(proposition.spotNumber, listForSpotNumber)
                }
            }
        }
        val iterator = mapText.keys.iterator()
        while (iterator.hasNext()) {
            val spotNumber = iterator.next()
            val listForSpotNumber = mapText.get(spotNumber)
            val mapSpot = hashMapOf<String, ArrayList<LocalDate>>()
            listForSpotNumber!!.forEach { proposition ->
                run {
                    val dates = mapSpot.getOrDefault(proposition.userId, arrayListOf())
                    dates.add(proposition.day)
                    mapSpot.put(proposition.userId, dates)
                }
            }
            message += ":parking: *${spotNumber}* :arrow_right: "
            val iteratorSpot = mapSpot.keys.iterator()
            while (iteratorSpot.hasNext()) {
                val userId = iteratorSpot.next()
                val dates = mapSpot.get(userId)
                message += " <@${userId}|${sortedActiveUsers.find { it.id == userId }!!.username}> [ "
                dates!!.forEach { message += "${it.format(DateTimeFormatter.ofPattern("dd/MM"))} " }
            }
            message += "]\n"
        }
        return message
    }

    fun generateTextForSchedule(schedules: List<Schedule>, userId: String?): String {
        var message = ""


        schedules.forEach { schedule ->
            run {
                var display = false

                message += "`${schedule.date.format(DateTimeFormatter.ofPattern("dd/MM"))} :` \n"
                schedule.assignedSpots.sortedBy { it.spotNumber }.forEach({ spot ->
                    run {
                        if (userId == null || userId == spot.userId) {
                            display = true
                            message += ":parking: ${spot.spotNumber} :arrow_right: <@${spot.userId}|${spot.username}> \n"
                        }
                    }
                })
                schedule.freeSpots.sorted().forEach({ spotNumber ->
                    run {
                        display = true
                        message += ":parking: ${spotNumber} :arrow_right: FREE :desert_island: \n"
                    }
                })
                if (!display){
                    message += "No Spot available.\n"
                }
            }
        }
        message += "\n"
        return message
    }

    fun requestCreated(user: User, date: LocalDate) {
        val message = Message("A spot is requested the ${date.format(DateTimeFormatter.ofPattern("dd/MM"))} by <@${user.id}|${user.username}>.")
        val restTemplate = RestTemplate()
        try {
            restTemplate.postForEntity<String>(slackWebhookUrl, message, String::class.java)
        } catch (e: RestClientException) {
            logger.error("Error posting to Slack Incoming Webhook: ", e)
        }
    }

}