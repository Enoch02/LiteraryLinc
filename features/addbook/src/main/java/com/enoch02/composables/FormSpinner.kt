package com.enoch02.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun FormSpinner(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier.padding(vertical = 8.dp)
) {
    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Spinner(
            selectedValue = selectedOption,
            values = options,
            onSelectionChange = onSelectionChange
        )
    }
}