package ru.criticaldays.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.criticaldays.app.data.CriticalDay

class CriticalDaysViewModel(private val repo: CriticalDaysRepository) : ViewModel() {

    val days = repo.observeDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDay(epochDay: Long, note: String) {
        viewModelScope.launch { repo.addDay(epochDay, note) }
    }

    fun delete(day: CriticalDay) {
        viewModelScope.launch { repo.delete(day) }
    }

    companion object {
        fun factory(repo: CriticalDaysRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CriticalDaysViewModel(repo) as T
            }
        }
    }
}
