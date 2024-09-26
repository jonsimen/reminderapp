package com.example.reminderapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reminderapp.ui.theme.ReminderappTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReminderappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    TaskManager()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TaskManager() {
    var taskText by remember { mutableStateOf("") }
    var taskList by remember { mutableStateOf(listOf<String>()) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarText by remember { mutableStateOf("") }

    // Getting an instance of Calendar to fetch the current date
// Source: https://www.baeldung.com/java-calendar
    val calendar = Calendar.getInstance()

// Getting the current year, month, and day from the Calendar
// Source: https://stackoverflow.com/questions/5369682/how-to-get-current-time-and-date-in-android
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        LocalContext.current,
        { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

// TimePickerDialog to select a time
// Source: https://www.tutorialspoint.com/how-to-use-timepickerdialog-in-kotlin
    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        { _, hourOfDay, minute ->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )


    Column(modifier = Modifier.padding(16.dp)) {
        // Input TextField for Task
        TextField(
            value = taskText,
            onValueChange = { taskText = it },
            label = { Text("Enter Task") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))


        Button(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Date")
        }

        Spacer(modifier = Modifier.height(8.dp))


        Button(onClick = { timePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
            Text("Select Time")
        }

        Spacer(modifier = Modifier.height(8.dp))


        Button(
            onClick = {
                if (taskText.isNotBlank() && selectedDate.isNotBlank() && selectedTime.isNotBlank()) {
                    taskList = taskList + "$taskText at $selectedDate $selectedTime"

                    val selectedDateTimeInMillis = getSelectedDateTimeInMillis(selectedDate, selectedTime)
                    val currentTimeInMillis = System.currentTimeMillis() // Use System.currentTimeMillis() instead of Calendar

                    val delayInMillis = selectedDateTimeInMillis - currentTimeInMillis

                    Log.d("TaskManager", "Delay in millis: $delayInMillis")

                    if (delayInMillis > 0) {
                        // Use Handler to schedule the Snackbar
                        scheduleSnackbar(delayInMillis, taskText, selectedDate, selectedTime, onSnackbarTrigger = {
                            snackbarText = "Reminder: $taskText at $selectedDate $selectedTime"
                            showSnackbar = true
                        })
                    }

                    taskText = "" // Clear input
                    selectedDate = ""
                    selectedTime = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { taskList = emptyList() }, modifier = Modifier.fillMaxWidth()) {
            Text("Delete All Tasks")
        }

        Spacer(modifier = Modifier.height(16.dp))


        LazyColumn {
            items(taskList) { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(task)
                    Checkbox(
                        checked = false,
                        onCheckedChange = { checked ->
                            if (checked) {
                                taskList = taskList - task
                            }
                        }
                    )
                    Text("Complete")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (showSnackbar) {
            Snackbar(
                action = {
                    Button(onClick = { showSnackbar = false }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(snackbarText)
            }
        }
    }
}


// convert selected date and time string into milliseconds
// Source: https://developer.android.com/reference/java/util/Calendar
fun getSelectedDateTimeInMillis(selectedDate: String, selectedTime: String): Long {
    // Split the selected date and time strings
    // Source: https://developer.android.com/reference/java/util/Calendar#set(int,%20int,%20int,%20int,%20int)
    val (day, month, year) = selectedDate.split("/").map { it.toInt() }
    val (hour, minute) = selectedTime.split(":").map { it.toInt() }

    // Set the calendar fields
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return calendar.timeInMillis
}



fun scheduleSnackbar(delayInMillis: Long, taskText: String, selectedDate: String, selectedTime: String, onSnackbarTrigger: () -> Unit) {
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
        onSnackbarTrigger()
        Log.d("TaskManager", "Snackbar triggered for task: $taskText")
    }, delayInMillis)
}