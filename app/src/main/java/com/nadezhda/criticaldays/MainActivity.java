package com.nadezhda.criticaldays;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "critical_days_prefs";
    private static final String KEY_MARKED_DATES = "marked_dates";

    private final SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    private TextView selectedDateText;
    private TextView markedDaysText;
    private Button toggleMarkButton;

    private String selectedDateIso;
    private final Set<String> markedDates = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalendarView calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        markedDaysText = findViewById(R.id.markedDaysText);
        toggleMarkButton = findViewById(R.id.toggleMarkButton);

        markedDates.addAll(loadMarkedDates());
        selectedDateIso = storageFormat.format(new Date(calendarView.getDate()));

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Date date = buildDate(year, month, dayOfMonth);
            selectedDateIso = storageFormat.format(date);
            updateUi();
        });

        toggleMarkButton.setOnClickListener(v -> {
            if (markedDates.contains(selectedDateIso)) {
                markedDates.remove(selectedDateIso);
                Toast.makeText(this, R.string.removed_ok, Toast.LENGTH_SHORT).show();
            } else {
                markedDates.add(selectedDateIso);
                Toast.makeText(this, R.string.saved_ok, Toast.LENGTH_SHORT).show();
            }
            saveMarkedDates(markedDates);
            updateUi();
        });

        updateUi();
    }

    private void updateUi() {
        String selectedDisplay = formatForDisplay(selectedDateIso);
        selectedDateText.setText(getString(R.string.selected_date_value, selectedDisplay));

        if (markedDates.contains(selectedDateIso)) {
            toggleMarkButton.setText(R.string.unmark_selected_day);
        } else {
            toggleMarkButton.setText(R.string.toggle_mark);
        }

        if (markedDates.isEmpty()) {
            markedDaysText.setText(R.string.no_marked_days);
            return;
        }

        List<String> sorted = new ArrayList<>(markedDates);
        Collections.sort(sorted);
        StringBuilder builder = new StringBuilder();
        for (String value : sorted) {
            builder.append("• ").append(formatForDisplay(value)).append('\n');
        }

        markedDaysText.setText(builder.toString().trim());
    }

    private Date buildDate(int year, int month, int dayOfMonth) {
        String raw = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
        try {
            return storageFormat.parse(raw);
        } catch (Exception e) {
            return new Date();
        }
    }

    private String formatForDisplay(String isoDate) {
        try {
            Date date = storageFormat.parse(isoDate);
            if (date == null) {
                return isoDate;
            }
            return displayFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private Set<String> loadMarkedDates() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet(KEY_MARKED_DATES, new HashSet<>()));
    }

    private void saveMarkedDates(Set<String> dates) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putStringSet(KEY_MARKED_DATES, new HashSet<>(dates)).apply();
    }
}
