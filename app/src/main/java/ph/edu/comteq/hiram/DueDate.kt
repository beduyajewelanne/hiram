package ph.edu.comteq.hiram

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

fun Record.withDueStatus(): Record {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    val due = LocalDate.parse(dueDate, formatter)
    val days = ChronoUnit.DAYS.between(LocalDate.now(), due)

    val (info, newStatus) = when {
        days < 0 -> "Overdue · ${-days} day" + (if (-days == 1L) "" else "s") to "Overdue"
        days == 0L -> "Due today" to "Active"
        else -> "Due in $days day" + (if (days == 1L) "" else "s") to "Active"
    }
    return copy(dueInfo = info, status = newStatus)
}