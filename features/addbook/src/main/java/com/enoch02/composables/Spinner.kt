package com.enoch02.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.enoch02.addbook.R

@Composable
internal fun Spinner(
    selectedValue: String,
    values: List<String>,
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        readOnly = true,
        value = selectedValue,
        onValueChange = { },
        trailingIcon = {
            IconButton(
                onClick = { expanded = !expanded },
                content = {
                    Icon(
                        painter = if (!expanded) {
                            painterResource(R.drawable.round_arrow_drop_down_24)
                        } else {
                            painterResource(R.drawable.baseline_arrow_drop_up_24)
                        }, contentDescription = null
                    )
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
        content = {
            values.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(text = selectionOption)
                    },
                    onClick = {
                        onSelectionChange(selectionOption)
                        expanded = false
                    }
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}