package com.enoch02.literarylinc.ui.components.drawersheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.R

@Composable
fun BookListDrawerSheet(
    selectedStatusFilter: StatusFilter,
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