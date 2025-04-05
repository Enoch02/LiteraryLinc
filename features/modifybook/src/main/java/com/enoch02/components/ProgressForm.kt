package com.enoch02.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLString

@Composable
fun ProgressForm(
    pagesRead: String,
    onPagesReadChange: (String) -> Unit,
    pageCount: String,
    onPageCountChange: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            content = {
                FormIntField(
                    label = stringResource(LLString.pagesRead),
                    value = pagesRead,
                    onValueChange = onPagesReadChange,
                    modifier = Modifier.weight(0.45f)
                )
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .weight(0.1f)
                )
                FormIntField(
                    label = stringResource(LLString.pageCount),
                    value = pageCount,
                    onValueChange = onPageCountChange,
                    modifier = Modifier.weight(0.45f)
                )
            }
        )
    }
}