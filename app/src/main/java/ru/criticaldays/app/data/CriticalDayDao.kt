package ru.criticaldays.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CriticalDayDao {
    @Query("SELECT * FROM critical_days ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<CriticalDay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: CriticalDay): Long

    @Delete
    suspend fun delete(day: CriticalDay)
}
