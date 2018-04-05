package io.saagie.astonparking.service

import io.saagie.astonparking.domain.Proposition
import io.saagie.astonparking.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class EmailService(
        val mailSender: JavaMailSender,
        val templateEngine: TemplateEngine) {

    @Value("\${sendEmail:true}")
    val sendEmail = true

    @Value("\${mailFrom}")
    val mailFrom = ""

    @Value("\${url}")
    val url = ""


    @Async
    fun profileCreated(user: User) {
        val context = Context()
        context.setVariable("user", user)
        context.setVariable("url", url)

        val messagePreparator = MimeMessagePreparator { mimeMessage ->
            val messageHelper = MimeMessageHelper(mimeMessage)
            messageHelper.setTo(user.email!!)
            messageHelper.setFrom(mailFrom)
            messageHelper.setSubject("Account created")
            messageHelper.setText(templateEngine.process("accountCreated", context), true)
        }

        send(messagePreparator)
    }

    @Async
    fun profileStatusChange(user: User) {
        val context = Context()
        context.setVariable("user", user)
        context.setVariable("url", url)

        val messagePreparator = MimeMessagePreparator { mimeMessage ->
            val messageHelper = MimeMessageHelper(mimeMessage)
            messageHelper.setTo(user.email!!)
            messageHelper.setFrom(mailFrom)
            messageHelper.setSubject("Status Change")
            messageHelper.setText(templateEngine.process("statusChange", context), true)
        }

        send(messagePreparator)
    }

    @Async
    fun proposition(propositions: List<Proposition>, sortedActiveUsers: List<User>) {
        val context = Context()
        context.setVariable("url", url)

        sortedActiveUsers.forEach { user ->
            run {
                context.setVariable("user", user)

                val propositionsForUser = propositions.filter { it.userId == user.id }
                if (!propositionsForUser.isEmpty()) {
                    val messagePreparator = MimeMessagePreparator { mimeMessage ->
                        context.setVariable("spotNumber", propositionsForUser.first().spotNumber)
                        context.setVariable("startDay", propositionsForUser.first().day.format(DateTimeFormatter.ofPattern("dd/MM")))
                        context.setVariable("endDay", propositionsForUser.last().day.format(DateTimeFormatter.ofPattern("dd/MM")))
                        val messageHelper = MimeMessageHelper(mimeMessage)
                        messageHelper.setTo(user.email!!)
                        messageHelper.setFrom(mailFrom)
                        messageHelper.setSubject("Spot attribution")
                        messageHelper.setText(templateEngine.process("spotAttribution", context), true)
                    }
                    send(messagePreparator)
                }
            }
        }

    }

    @Async
    fun pickAfterRequest(user: User, spotNumber: Int, day: LocalDate) {
        val context = Context()
        context.setVariable("url", url)
        val messagePreparator = MimeMessagePreparator { mimeMessage ->
            val messageHelper = MimeMessageHelper(mimeMessage)
            messageHelper.setTo(user.email!!)
            messageHelper.setFrom(mailFrom)
            context.setVariable("spotNumber", spotNumber)
            context.setVariable("user", user)
            context.setVariable("day", day.format(DateTimeFormatter.ofPattern("dd/MM")))
            messageHelper.setSubject("Spot attribution after a request")
            messageHelper.setText(templateEngine.process("spotAttributionAfterRequest", context), true)
        }
        send(messagePreparator)
    }

    private fun send(messagePreparator: MimeMessagePreparator) {
        try {
            if (sendEmail) {
                mailSender.send(messagePreparator)
            }
        } catch (e: Exception) {

        }
    }
}