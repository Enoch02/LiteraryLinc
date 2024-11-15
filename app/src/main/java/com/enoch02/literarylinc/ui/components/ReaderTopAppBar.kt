package com.enoch02.literarylinc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.enoch02.database.model.ReaderFilter
import com.enoch02.literarylinc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopAppBar(
    readerFilter: ReaderFilter,
    onSearch: () -> Unit,
    onShowSorting: () -> Unit,
    onChangeDrawerState: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = readerFilter.value,
                fontStyle = MaterialTheme.typography.titleLarge.fontStyle
            )
        },
        actions = {
            IconButton(
                onClick = { onSearch() },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(R.string.search_desc)
                    )
                }
            )

            IconButton(
                onClick = { onShowSorting() },
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
                        contentDescription = stringResource(R.string.open_menu)
                    )
                }
            )
        }
    )
}
