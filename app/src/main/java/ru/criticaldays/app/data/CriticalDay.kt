package ru.criticaldays.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "critical_days")
data class CriticalDay(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val note: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
)
