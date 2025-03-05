package com.enoch02.components

import androidx.compose.animation.AnimatedVisibility
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
import com.enoch02.addbook.R
import com.enoch02.database.model.Book

@Composable
fun ProgressForms(
    type: String,
    pagesRead: String,
    onPagesReadChange: (String) -> Unit,
    pageCount: String,
    onPageCountChange: (String) -> Unit,
    volumesRead: String,
    onVolumesReadChange: (String) -> Unit,
    totalVolumes: String,
    onTotalVolumesChange: (String) -> Unit
) {
    Column {
//        val types = Book.types.values.toList()
        val types = Book.Companion.BookType.entries.map { it.strName }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            content = {
                FormIntField(
                    label = stringResource(R.string.pages_read_label),
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
                    label = stringResource(R.string.page_count_label),
                    value = pageCount,
                    onValueChange = onPageCountChange,
                    modifier = Modifier.weight(0.45f)
                )
            }
        )

//        AnimatedVisibility(
//            visible = type == types[0] || type == types[2] || type == types[4],
//            content = {
//
//            }
//        )

        //TODO: fix? or remove
        AnimatedVisibility(
//            visible = type == types[1] || type == types[2] || type == types[3],
            visible = false,
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    content = {
                        FormIntField(
                            label = stringResource(R.string.volumes_read_label),
                            value = volumesRead,
                            onValueChange = onVolumesReadChange,
                            modifier = Modifier.weight(0.45f)
                        )
                        Spacer(
                            modifier = Modifier
                                .width(4.dp)
                                .weight(0.1f)
                        )
                        FormIntField(
                            label = stringResource(R.string.total_volumes_label),
                            value = totalVolumes,
                            onValueChange = onTotalVolumesChange,
                            modifier = Modifier.weight(0.45f)
                        )
                    }
                )
            }
        )
    }
}