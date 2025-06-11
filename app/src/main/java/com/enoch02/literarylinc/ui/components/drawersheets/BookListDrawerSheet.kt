package com.enoch02.literarylinc.ui.components.drawersheets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enoch02.database.model.StatusFilter

@Composable
fun BookListDrawerSheet(
    selectedStatusFilter: StatusFilter?,
    onStatusSelected: (StatusFilter) -> Unit
) {
    val statusFilters = StatusFilter.entries.toTypedArray()

    ModalDrawerSheet(
        content = {
            SheetHeader()

            LazyColumn(
                content = {
                    items(statusFilters) {
                        NavigationDrawerItem(
                            label = {
                                Text(text = it.stringify())
                            },
                            icon = {
                                Icon(
                                    imageVector = when (it) {
                                        StatusFilter.ALL -> {
                                            Icons.AutoMirrored.Rounded.MenuBook
                                        }

                                        StatusFilter.READING -> {
                                            Icons.AutoMirrored.Rounded.ReadMore
                                        }

                                        StatusFilter.COMPLETED -> {
                                            Icons.Rounded.DoneAll
                                        }

                                        StatusFilter.ON_HOLD -> {
                                            Icons.Rounded.PauseCircle
                                        }

                                        StatusFilter.PLANNING -> {
                                            Icons.Rounded.Schedule
                                        }

                                        StatusFilter.REREADING -> {
                                            Icons.Rounded.RestartAlt
                                        }
                                    },
                                    contentDescription = null
                                )
                            },
                            selected = selectedStatusFilter == it,
                            onClick = {
                                onStatusSelected(it)
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