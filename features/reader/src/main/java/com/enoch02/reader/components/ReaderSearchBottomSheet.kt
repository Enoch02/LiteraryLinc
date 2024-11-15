package com.enoch02.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.tooling.preview.Preview
import com.enoch02.database.model.LLDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSearchBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> List<LLDocument>
) {
    var query by remember { mutableStateOf("") }

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { isTraversalGroup = true }) {
                SearchBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query,
                            onQueryChange = {
                                query = it
                            },
                            onSearch = {
                                /*TODO*/
                            },
                            expanded = true,
                            onExpandedChange = {},
                            modifier = Modifier,
                            enabled = true,
                            placeholder = { Text("Book Title") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { onDismiss() }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null
                                    )
                                }
                            },
                        )
                    },
                    expanded = true,
                    onExpandedChange = { }
                ) {
                    LazyColumn {
                        items(100) {
                            ListItem(headlineContent = { Text("item $it") })
                        }
                    }
                }
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    //TODO
    /*Column {
        Text("Pre")
        SearchBottomSheet(visible = true) { }
    }*/
}