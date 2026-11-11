package ph.edu.comteq.hiram

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface RecordDao {
    @Insert
    suspend fun insert(record: Record)

    @Delete
    suspend fun delete(record: Record)

    @Update
    suspend fun update(record: Record)

    @Query("""
    UPDATE records
    SET status = 'Returned', dueInfo = :dueInfo, returnedOn = :returnedOn
    WHERE id = :recordId """
    )
    suspend fun markReturned(
        recordId: Int, dueInfo: String, returnedOn: String
    ): Int

    @Query("SELECT * FROM records ORDER BY id DESC")
    fun observeAll(): Flow<List<Record>>
}