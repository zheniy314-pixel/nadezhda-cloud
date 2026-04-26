package com.nadezhda.criticaldays

import android.content.Context
import android.os.Bundle
import android.widget.CalendarView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeSet

class MainActivity : AppCompatActivity() {

    private val prefsName = "critical_days_prefs"
    private val keyMarkedDays = "marked_days"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("ru", "RU"))

    private lateinit var calendarView: CalendarView
    private lateinit var dateHint: TextView
    private lateinit var toggleButton: MaterialButton
    private lateinit var criticalDaysText: TextView

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val markedDays = TreeSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarView = findViewById(R.id.calendarView)
        dateHint = findViewById(R.id.dateHint)
        toggleButton = findViewById(R.id.toggleButton)
        criticalDaysText = findViewById(R.id.criticalDaysText)

        loadMarkedDays()
        selectedDateMillis = calendarView.date
        updateUi()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDateMillis = buildDateMillis(year, month, dayOfMonth)
            updateUi()
        }

        toggleButton.setOnClickListener {
            val date = currentDateKey()
            if (markedDays.contains(date)) {
                markedDays.remove(date)
            } else {
                markedDays.add(date)
            }
            saveMarkedDays()
            updateUi()
        }
    }

    private fun updateUi() {
        val currentDate = currentDateKey()
        val isMarked = markedDays.contains(currentDate)
        dateHint.text = if (isMarked) {
            getString(R.string.selected_day_marked, currentDate)
        } else {
            getString(R.string.selected_day_not_marked, currentDate)
        }

        toggleButton.text = if (isMarked) {
            getString(R.string.unmark_critical_day)
        } else {
            getString(R.string.mark_as_critical)
        }

        criticalDaysText.text = if (markedDays.isEmpty()) {
            getString(R.string.no_critical_days)
        } else {
            getString(
                R.string.critical_days_with_count,
                markedDays.size,
                markedDays.joinToString(separator = "\n")
            )
        }
    }

    private fun currentDateKey(): String {
        return dateFormat.format(Date(selectedDateMillis))
    }

    private fun loadMarkedDays() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(keyMarkedDays, emptySet()) ?: emptySet()
        markedDays.clear()
        markedDays.addAll(stored)
    }

    private fun saveMarkedDays() {
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        prefs.edit()
            .putStringSet(keyMarkedDays, HashSet(markedDays))
            .apply()
    }

    private fun buildDateMillis(year: Int, month: Int, dayOfMonth: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth, 12, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
