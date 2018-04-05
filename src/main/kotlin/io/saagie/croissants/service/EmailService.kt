package io.saagie.croissants.service

import io.saagie.croissants.domain.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context


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

    private fun send(messagePreparator: MimeMessagePreparator) {
        try {
            if (sendEmail) {
                mailSender.send(messagePreparator)
            }
        } catch (e: Exception) {

        }
    }
}