package com.enoch02.components

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enoch02.addbook.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormDatePicker(
    modifier: Modifier,
    label: String,
    datePickerState: DatePickerState
) {
    var showDialog by remember { mutableStateOf(false) }
    var formattedDate by rememberSaveable { mutableStateOf("") }

    val formatDate: () -> Unit = {
        formattedDate =
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && datePickerState.selectedDateMillis != null -> {
                    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                    val date = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(datePickerState.selectedDateMillis!!),
                        ZoneId.systemDefault()
                    )
                    formatter.format(date)
                }

                Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && datePickerState.selectedDateMillis != null -> {
                    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
                    formatter.format(Date(datePickerState.selectedDateMillis!!))
                }

                else -> {
                    ""
                }
            }
    }
    formatDate()

    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            readOnly = true,
            value = formattedDate,
            onValueChange = { },
            trailingIcon = {
                IconButton(
                    onClick = { showDialog = true },
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.round_arrow_drop_down_24),
                            contentDescription = null
                        )
                    }
                )
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
        )

        if (showDialog) {
            val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

            DatePickerDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            formatDate()
                        },
                        enabled = confirmEnabled,
                        content = { Text(text = stringResource(R.string.ok)) }
                    )
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        content = {
                            Text(text = stringResource(R.string.cancel))
                        }
                    )
                },
                content = {
                    DatePicker(state = datePickerState)
                }
            )
        }
    }
}