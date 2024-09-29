package com.enoch02.more.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun SwitchSettingItem(
    label: String,
    description: String = "",
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(text = label)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckChanged
            )
        },
        supportingContent = {
            if (description.isNotEmpty()) {
                Text(text = description)
            }
        },
        modifier = Modifier.clickable {
            onCheckChanged(!checked)
        },
        tonalElevation = 30.dp
    )
}