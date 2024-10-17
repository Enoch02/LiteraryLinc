package com.enoch02.literarylinc.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.enoch02.booklist.components.BookListViewMode
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListTopAppBar(
    statusFilter: StatusFilter,
    bookListViewMode: BookListViewMode,
    onChangeBookListMode: (mode: BookListViewMode) -> Unit,
    onShowSortOption: (show: Boolean) -> Unit,
    onChangeDrawerState: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = when (statusFilter) {
                    StatusFilter.ALL -> {
                        stringResource(id = R.string.your_library_label)
                    }

                    StatusFilter.READING -> {
                        StatusFilter.READING.stringify()
                    }

                    StatusFilter.COMPLETED -> {
                        StatusFilter.COMPLETED.stringify()
                    }

                    StatusFilter.ON_HOLD -> {
                        StatusFilter.ON_HOLD.stringify()
                    }

                    StatusFilter.PLANNING -> {
                        StatusFilter.PLANNING.stringify()
                    }
                }
            )
        },
        actions = {
            IconButton(
                onClick = {
                    when (bookListViewMode) {
                        BookListViewMode.LIST_VIEW -> {
                            onChangeBookListMode(BookListViewMode.GRID_VIEW)
                        }

                        BookListViewMode.GRID_VIEW -> {
                            onChangeBookListMode(BookListViewMode.LIST_VIEW)
                        }
                    }
                },
                content = {
                    Icon(
                        painter = when (bookListViewMode) {
                            BookListViewMode.LIST_VIEW -> {
                                painterResource(id = R.drawable.round_grid_view_24)
                            }

                            BookListViewMode.GRID_VIEW -> {
                                painterResource(id = R.drawable.round_view_list_24)
                            }
                        },
                        contentDescription = stringResource(R.string.toggle_arrangement_desc)
                    )
                }
            )

            IconButton(
                onClick = { onShowSortOption(true) },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_sort_24),
                        contentDescription = stringResource(R.string.sort_desc)
                    )
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onChangeDrawerState()
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_menu_24),
                        contentDescription = stringResource(R.string.status_filter_menu_desc)
                    )
                }
            )
        }
    )
}