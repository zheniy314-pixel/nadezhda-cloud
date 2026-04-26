package com.evgeny.criticaldays;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "critical_days_prefs";
    private static final String KEY_MARKED_DAYS = "marked_days";

    private final Set<String> markedDays = new HashSet<>();

    private TextView selectedDateValue;
    private TextView selectedDateStatus;
    private Button toggleDayButton;
    private ArrayAdapter<String> markedDaysAdapter;

    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalendarView calendarView = findViewById(R.id.calendarView);
        selectedDateValue = findViewById(R.id.selectedDateValue);
        selectedDateStatus = findViewById(R.id.selectedDateStatus);
        toggleDayButton = findViewById(R.id.toggleDayButton);
        ListView markedDaysList = findViewById(R.id.markedDaysList);

        loadMarkedDays();

        markedDaysAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                buildSortedMarkedDaysList()
        );
        markedDaysList.setAdapter(markedDaysAdapter);

        selectedDate = formatMillisToDate(calendarView.getDate());
        renderSelectedDateState();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            renderSelectedDateState();
        });

        toggleDayButton.setOnClickListener(v -> {
            if (markedDays.contains(selectedDate)) {
                markedDays.remove(selectedDate);
            } else {
                markedDays.add(selectedDate);
            }
            saveMarkedDays();
            refreshMarkedDaysList();
            renderSelectedDateState();
        });
    }

    private void loadMarkedDays() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> stored = prefs.getStringSet(KEY_MARKED_DAYS, new HashSet<>());
        markedDays.clear();
        markedDays.addAll(stored == null ? new HashSet<>() : stored);
    }

    private void saveMarkedDays() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putStringSet(KEY_MARKED_DAYS, new HashSet<>(markedDays))
                .apply();
    }

    private void refreshMarkedDaysList() {
        markedDaysAdapter.clear();
        markedDaysAdapter.addAll(buildSortedMarkedDaysList());
        markedDaysAdapter.notifyDataSetChanged();
    }

    private List<String> buildSortedMarkedDaysList() {
        List<String> list = new ArrayList<>(markedDays);
        Collections.sort(list);
        return list;
    }

    private void renderSelectedDateState() {
        selectedDateValue.setText(getString(R.string.selected_date_template, selectedDate));
        boolean isMarked = markedDays.contains(selectedDate);
        selectedDateStatus.setText(isMarked
                ? getString(R.string.status_marked)
                : getString(R.string.status_not_marked));
        toggleDayButton.setText(isMarked
                ? getString(R.string.button_remove_day)
                : getString(R.string.button_add_day));
    }

    private String formatMillisToDate(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
}
