package ph.edu.comteq.hiram

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
