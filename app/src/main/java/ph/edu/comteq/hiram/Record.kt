package ph.edu.comteq.hiram

data class Record(
    val id: Int,
    val kind: String,
    val title: String,
    val amount: Double,
    val person: String,
    val type: String,
    val dueDate: String,
    val dueInfo: String,
    val status: String,
    val note: String,
    val returnedOn: String
)
