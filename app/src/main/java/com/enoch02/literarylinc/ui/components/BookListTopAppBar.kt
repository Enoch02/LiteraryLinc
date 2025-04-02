package com.enoch02.literarylinc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.enoch02.booklist.components.BookViewMode
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.R
import com.enoch02.resources.LLString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListTopAppBar(
    statusFilter: StatusFilter,
    bookViewMode: BookViewMode,
    onChangeBookListMode: (mode: BookViewMode) -> Unit,
    onShowSortOption: (show: Boolean) -> Unit,
    onChangeDrawerState: () -> Unit,
    onSearch: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = when (statusFilter) {
                    StatusFilter.ALL -> {
                        stringResource(id = LLString.yourLibrary)
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

                    StatusFilter.REREADING -> {
                        StatusFilter.REREADING.stringify()
                    }
                }
            )
        },
        actions = {
            IconButton(
                onClick = { onSearch() },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(LLString.search)
                    )
                }
            )

            IconButton(
                onClick = {
                    when (bookViewMode) {
                        BookViewMode.LIST_VIEW -> {
                            onChangeBookListMode(BookViewMode.GRID_VIEW)
                        }

                        BookViewMode.GRID_VIEW -> {
                            onChangeBookListMode(BookViewMode.LIST_VIEW)
                        }
                    }
                },
                content = {
                    Icon(
                        painter = when (bookViewMode) {
                            BookViewMode.LIST_VIEW -> {
                                painterResource(id = R.drawable.round_grid_view_24)
                            }

                            BookViewMode.GRID_VIEW -> {
                                painterResource(id = R.drawable.round_view_list_24)
                            }
                        },
                        contentDescription = stringResource(LLString.toggleBooklistArrangementDesc)
                    )
                }
            )

            IconButton(
                onClick = { onShowSortOption(true) },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_sort_24),
                        contentDescription = stringResource(LLString.sort)
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
                        contentDescription = stringResource(LLString.statusFilterDesc)
                    )
                }
            )
        }
    )
}