package com.enoch02.stats.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLString

@Composable
fun EditReadingGoalDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    goal: String,
    progress: String,
    onDismiss: () -> Unit,
    onSave: (goal: Int, progress: Int) -> Unit
) {
    val context = LocalContext.current
    var mGoal by remember { mutableStateOf("") }
    var mProgress by remember { mutableStateOf("") }
    val saveChanges = {
        try {
            val gInt = mGoal.toInt()
            val pInt = mProgress.toInt()

            if (pInt >= gInt) {
                Toast.makeText(
                    context,
                    "Your progress can't be greater than the goal!🤨",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                onSave(gInt, pInt)
                onDismiss()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        mGoal = goal
        mProgress = progress
    }

    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(LLString.modifyReadingGoal)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = mGoal,
                        onValueChange = { mGoal = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(stringResource(LLString.readingGoal)) }
                    )

                    Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = mProgress,
                        onValueChange = { mProgress = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                saveChanges()
                            }
                        ),
                        label = { Text(stringResource(LLString.progress)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { saveChanges() },
                    content = { Text(stringResource(LLString.save)) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = { Text(stringResource(LLString.cancel)) }
                )
            },
            modifier = modifier
        )
    }
}

@Preview
@Composable
private fun Preview() {
    EditReadingGoalDialog(
        visible = true,
        onDismiss = {},
        progress = "5",
        goal = "999",
        onSave = { _, _ -> })
}