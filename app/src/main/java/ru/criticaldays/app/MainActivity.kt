package ru.criticaldays.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.criticaldays.app.data.CriticalDay
import ru.criticaldays.app.data.AppDatabase
import ru.criticaldays.app.ui.theme.CriticalDaysTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = AppDatabase.create(applicationContext)
        val repo = CriticalDaysRepository(db)
        setContent {
            CriticalDaysTheme {
                val vm: CriticalDaysViewModel = viewModel(factory = CriticalDaysViewModel.factory(repo))
                CriticalDaysScreen(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriticalDaysScreen(vm: CriticalDaysViewModel) {
    val days by vm.days.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Критические дни") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить день")
            }
        }
    ) { padding ->
        if (days.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Пока нет записей.\nНажми + чтобы отметить день.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(days, key = { it.id }) { day ->
                    DayRow(day = day, onDelete = { vm.delete(day) })
                }
            }
        }
    }

    if (showAdd) {
        AddDayDialog(
            onDismiss = { showAdd = false },
            onConfirm = { epochDay, note ->
                vm.addDay(epochDay, note)
                showAdd = false
            }
        )
    }
}

private val zone: ZoneId = ZoneId.systemDefault()
private val displayFormat: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

@Composable
private fun DayRow(day: CriticalDay, onDelete: () -> Unit) {
    val date = LocalDate.ofEpochDay(day.dateEpochDay)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = date.format(displayFormat), style = MaterialTheme.typography.titleMedium)
                if (day.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = day.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDayDialog(
    onDismiss: () -> Unit,
    onConfirm: (epochDay: Long, note: String) -> Unit
) {
    val todayMillis = LocalDate.now(zone)
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
    var note by remember { mutableStateOf("") }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis ?: todayMillis
                    val day = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
                    onConfirm(day.toEpochDay(), note)
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            DatePicker(state = pickerState)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4
            )
        }
    }
}
