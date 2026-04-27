package ru.criticaldays.app

import kotlinx.coroutines.flow.Flow
import ru.criticaldays.app.data.AppDatabase
import ru.criticaldays.app.data.CriticalDay

class CriticalDaysRepository(private val db: AppDatabase) {
    fun observeDays(): Flow<List<CriticalDay>> = db.criticalDayDao().observeAll()

    suspend fun addDay(epochDay: Long, note: String) {
        db.criticalDayDao().upsert(CriticalDay(dateEpochDay = epochDay, note = note.trim()))
    }

    suspend fun delete(day: CriticalDay) {
        db.criticalDayDao().delete(day)
    }
}
