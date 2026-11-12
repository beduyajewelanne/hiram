package ph.edu.comteq.hiram

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val dueFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

private fun dayText(n: Long):
        String = if (n == 1L) "1 day" else "$n days"

fun Record.dueLocalDate(): LocalDate? =
    try {
        LocalDate.parse(dueDate, dueFormat)
    } catch (e: Exception){
        null
    }
fun Record.withDueStatus(): Record {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    val due = LocalDate.parse(dueDate, formatter)
    val days = ChronoUnit.DAYS.between(LocalDate.now(), due)

    return when{
        days < 0 -> copy(dueInfo = "Overdue - ${dayText(-days)}", status = "Overdue")
        days == 0L -> copy(dueInfo = "Due today", status = "Active")
        else -> copy(dueInfo = "Due in ${dayText(days)}", status = "Active")
    }
}