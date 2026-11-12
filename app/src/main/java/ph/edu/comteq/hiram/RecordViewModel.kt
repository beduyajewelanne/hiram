package ph.edu.comteq.hiram

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecordViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RecordDatabase.getInstance(application).recordDao()

    val records: StateFlow<List<Record>> = dao.observeAll()
        .map { list -> list.map { it.withDueStatus() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addRecord(record: Record) {
        viewModelScope.launch {
            dao.insert(record.withDueStatus())
        }
    }

    fun updateRecord(record: Record){
        viewModelScope.launch {
            dao.update(record.withDueStatus())
        }
    }

    fun deleteRecord(record: Record){
        viewModelScope.launch {
            dao.delete(record)
        }
    }

    fun markReturned(record: Record){
        viewModelScope.launch {
            val today = LocalDate.now()
            val returnedOn =today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
            val shortDate =today.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
            dao.markReturned(record.id, "Settled - $shortDate", returnedOn)
        }
    }
}