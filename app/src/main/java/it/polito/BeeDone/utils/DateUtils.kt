package it.polito.BeeDone.utils

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
This class is needed for the DatePicker.
It performs date conversions
 */
class DateUtils {
    @RequiresApi(Build.VERSION_CODES.P)
    fun convertMillisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun convertMillisToLocalDateWithFormatter(
        date: LocalDate,
        dateTimeFormatter: DateTimeFormatter
    ): LocalDate {
        //Convert the date to a long in millis using a formatter
        val dateInMillis = LocalDate.parse(date.format(dateTimeFormatter), dateTimeFormatter)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        //Convert the millis to a localDate object
        return Instant.ofEpochMilli(dateInMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun dateToString(date: LocalDate): String {                 //Converts date into specified format
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val dateInMillis = convertMillisToLocalDateWithFormatter(date, dateFormatter)
        return dateFormatter.format(dateInMillis)
    }
}

/**
Manages the DatePicker for the BirthDate
 */
@SuppressLint("RememberReturnType")
@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerWithDialog(                           //Function that manages date picker
    dateValue: String,
    dateError: String,
    setDate: (String) -> Unit,
    labelValue: String,
    selectableDates: SelectableDates
) {
    val dateState = rememberDatePickerState(selectableDates = selectableDates)
    val millisToLocalDate = dateState.selectedDateMillis?.let {
        DateUtils().convertMillisToLocalDate(it)
    }
    val dateToString = millisToLocalDate?.let {
        DateUtils().dateToString(millisToLocalDate)
    } ?: dateValue


    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dateToString,
        onValueChange = { },
        isError = dateError.isNotBlank(),
        label = { Text(labelValue) },
        readOnly = true,
        shape = myShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = lightBlue,
            focusedLabelColor = lightBlue,
            focusedTextColor = Color.DarkGray
        ),
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = PointerEventPass.Initial)
                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                if (upEvent != null) {
                    showDialog = true
                }
            }
        }
    )

    CreateErrorText(dateError)
    Spacer(modifier = Modifier.height(16.dp))

    if (showDialog) {
        DatePickerDialog(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        setDate(dateToString)
                    }
                ) {
                    Text(text = "OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text(text = "Cancel")
                }
            }
        ) {
            DatePicker(
                state = dateState,
                showModeToggle = true
            )
        }
    }

}

/**
 * Used to specify which dates are selectable in the DatePicker. In this case, only  present and past dates are selectable
 */
@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates : SelectableDates {
    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= System.currentTimeMillis()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @ExperimentalMaterial3Api
    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}

/**
 * Same as above, but only dates in the future are selectable
 */
@OptIn(ExperimentalMaterial3Api::class)
object PresentOrFutureSelectableDates : SelectableDates {
    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis >= System.currentTimeMillis()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @ExperimentalMaterial3Api
    override fun isSelectableYear(year: Int): Boolean {
        return year >= LocalDate.now().year
    }
}
