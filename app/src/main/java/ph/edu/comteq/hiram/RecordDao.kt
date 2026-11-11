package ph.edu.comteq.hiram

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Query("SELECT * FROM records ORDER BY id DESC")
    fun observeAll(): Flow<List<Record>>
}