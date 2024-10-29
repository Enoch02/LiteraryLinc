package com.enoch02.literarylinc.ui.components.drawersheets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enoch02.database.model.ReaderFilter


@Composable
fun ReaderListDrawerSheet(
    selectedFilter: ReaderFilter,
    onFilterSelected: (ReaderFilter) -> Unit
) {
    val readerFilter = ReaderFilter.entries.toTypedArray()

    ModalDrawerSheet(
        content = {
            SheetHeader()

            LazyColumn(
                content = {
                    items(readerFilter) {
                        NavigationDrawerItem(
                            label = {
                                Text(text = it.value)
                            },
                            icon = {
                                Icon(
                                    imageVector = when (it) {
                                        ReaderFilter.READING -> {
                                            Icons.AutoMirrored.Rounded.ReadMore
                                        }

                                        ReaderFilter.FAVORITES -> {
                                            Icons.Rounded.StarOutline
                                        }

                                        ReaderFilter.COMPLETED -> {
                                            Icons.Rounded.DoneAll
                                        }

                                        ReaderFilter.ALL -> {
                                            Icons.AutoMirrored.Rounded.MenuBook
                                        }

                                        ReaderFilter.NO_FILE -> {
                                            Icons.Rounded.ErrorOutline
                                        }
                                    },
                                    contentDescription = null
                                )
                            },
                            selected = selectedFilter == it,
                            onClick = {
                                onFilterSelected(it)
                            },
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                bottomStart = 4.dp,
                                topEnd = 24.dp,
                                bottomEnd = 24.dp
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            )
        }
    )
}
