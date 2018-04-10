package io.saagie.croissants.service;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.*

@Service
class UtilService {

    fun extractDate(text: String): LocalDate {
        val now = LocalDate.now()

        try {
            val date = LocalDate.parse(text , DateTimeFormatter.ofPattern("dd/MM/uu"))
            if (now.isAfter(date)) {
                throw IllegalArgumentException("Date can't be before today")
            }
            return date
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Date format is not correct : dd/MM/YY (day/month/year)")
        }
    }
    fun localDateToDate(localDate: LocalDate): Date {

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    fun dateToLocalDate(date: Date): LocalDate{
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun getNextFriday(): LocalDate {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    }
}
