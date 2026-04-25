package com.simplifybiz.mobile.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.ktor.util.date.GMTDate
import io.ktor.util.date.Month

/**
 * Date / time picker dialogs for the Objectives module.
 *
 * Output formats are the strings the webapp Gravity Forms accepts:
 *   - Date: "YYYY-MM-DD"
 *   - Time: "hh:MM AM/PM"
 *
 * Both are KMP-safe — no kotlinx.datetime, no String.format.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateIso: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialMillis = remember(initialDateIso) { parseIsoDateToMillis(initialDateIso) }

    val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val picked = state.selectedDateMillis
                if (picked != null) {
                    onConfirm(formatMillisToIso(picked))
                } else {
                    onDismiss()
                }
            }) { Text("OK", color = Color(0xFF4A148C)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val (initHour, initMinute) = remember(initialTime) {
        parseTimeString(initialTime) ?: (17 to 0) // default 5:00 PM
    }

    val state = rememberTimePickerState(
        initialHour = initHour,
        initialMinute = initMinute,
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    Text("Select time", modifier = Modifier.padding(bottom = 12.dp))
                    TimePicker(state = state)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
                        Button(
                            onClick = { onConfirm(formatTime(state.hour, state.minute)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                        ) { Text("OK") }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------
// Parsing & formatting — KMP-safe (no String.format, no kotlinx.datetime)
// -------------------------------------------------------------------

private fun pad2(n: Int): String = n.toString().padStart(2, '0')
private fun pad4(n: Int): String = n.toString().padStart(4, '0')

private fun parseIsoDateToMillis(iso: String): Long? {
    if (iso.isBlank()) return null
    val parts = iso.split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    if (m !in 1..12 || d !in 1..31) return null
    return runCatching {
        GMTDate(
            seconds = 0,
            minutes = 0,
            hours = 0,
            dayOfMonth = d,
            month = Month.from(m - 1), // GMTDate Month is 0-indexed
            year = y
        ).timestamp
    }.getOrNull()
}

private fun formatMillisToIso(millis: Long): String {
    val date = GMTDate(millis)
    val month = date.month.ordinal + 1
    return "${pad4(date.year)}-${pad2(month)}-${pad2(date.dayOfMonth)}"
}

/** Accepts "hh:MM AM/PM", "HH:MM", or hour-only forms. Returns 24-h pair. */
private fun parseTimeString(s: String): Pair<Int, Int>? {
    if (s.isBlank()) return null
    val trimmed = s.trim().uppercase()
    val isPm = trimmed.endsWith("PM")
    val isAm = trimmed.endsWith("AM")
    val core = trimmed.removeSuffix("AM").removeSuffix("PM").trim()
    val parts = core.split(":")
    val rawHour = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val hour24 = when {
        isPm && rawHour < 12 -> rawHour + 12
        isAm && rawHour == 12 -> 0
        else -> rawHour
    }
    if (hour24 !in 0..23 || minute !in 0..59) return null
    return hour24 to minute
}

private fun formatTime(hour24: Int, minute: Int): String {
    val period = if (hour24 < 12) "AM" else "PM"
    val h12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "${pad2(h12)}:${pad2(minute)} $period"
}