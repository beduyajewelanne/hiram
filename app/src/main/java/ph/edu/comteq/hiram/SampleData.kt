package ph.edu.comteq.hiram

import java.util.Locale
fun amount(amount: Double): String {
    return "₱" + String.format(Locale.US, "%,.2f", amount)
}
fun recordName(record: Record): String {
    return if (record.kind == "Money") amount(record.amount) else record.title
}
fun date(date: String): String {
    val parts = date.split(" ")
    return if (parts.size >= 2) parts[0] + " " + parts[1].take(3) else date
}

fun populateRecordList(): List<Record> {
    return listOf(
        Record(1, "Item", "Camera", 0.0, "Jewel Anne Beduya", "Lent",
            "20 October 2026", "Overdue · 2 days", "Overdue",
            "Picture lang.", ""),
        Record(2, "Money", "", 850.0, "Angel Selpo", "Borrowed",
            "22 October 2026", "Due today", "Active", "", ""),
        Record(3, "Item", "Scientific calculator", 0.0, "Alexandra Cabrera", "Lent",
            "25 October 2026", "Due in 3 days", "Active", "", ""),
        Record(4, "Money", "", 1200.0, "Carlo Ramos", "Lent",
            "29 October 2026", "Due in 7 days", "Active", "", ""),
        Record(5, "Item", "Cordless drill", 0.0, "Tito Jun", "Borrowed",
            "2 November 2026", "Due in 11 days", "Active", "", ""),
        Record(6, "Money", "", 500.0, "Juliamay Nina", "Lent",
            "18 October 2026", "Settled · 17 Oct", "Returned", "", "17 October 2026")
    )
}