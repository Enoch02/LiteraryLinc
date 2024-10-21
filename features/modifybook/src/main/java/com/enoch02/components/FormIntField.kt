package com.enoch02.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.enoch02.addbook.R

@Composable
internal fun FormIntField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.isNotEmpty()) {
                    val filteredInput = it.filter { char -> char.isDigit() }
                    onValueChange(filteredInput)
                } else {
                    onValueChange(it)
                }

            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
            )
        )
    }
}

/***
 * [FormIntField] with extra buttons for incrementing
 * and decrementing its value.
 */
@Composable
internal fun IncrementalFormIntField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        FormIntField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(0.7f)
        )

        Row(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxSize()
                .align(Alignment.Bottom),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            FilledIconButton(
                onClick = {
                    val temp = value.toInt()
                    onDecrement(if (temp > 0) (temp - 1).toString() else "0")
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_minus_24),
                        contentDescription = stringResource(R.string.increment_desc)
                    )
                }
            )

            FilledIconButton(
                onClick = {
                    onIncrement((value.toInt() + 1).toString())
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_add_24),
                        contentDescription = stringResource(R.string.decrement_desc)
                    )
                }
            )
        }
    }
}
