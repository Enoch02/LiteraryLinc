package com.enoch02.stats.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.enoch02.resources.LLString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTopAppBar(
    readingGoal: Int,
    readingProgress: Int,
    onSaveProgressData: (goal: Int, progress: Int) -> Unit
) {
    var showGoalEditDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = stringResource(id = LLString.statistics)) },
        actions = {
            IconButton(
                onClick = { showGoalEditDialog = true },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(LLString.editReadingGoalDesc)
                    )
                }
            )
        }
    )

    EditReadingGoalDialog(
        title = stringResource(LLString.modifyReadingGoal),
        visible = showGoalEditDialog,
        goal = readingGoal,
        progress = readingProgress,
        onDismiss = { showGoalEditDialog = false },
        onSave = { goal, progress ->
            onSaveProgressData(goal, progress)
        }
    )
}