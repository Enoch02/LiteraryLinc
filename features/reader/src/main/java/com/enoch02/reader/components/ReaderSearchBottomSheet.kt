package com.enoch02.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.enoch02.database.model.LLDocument
import com.enoch02.resources.LLString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSearchBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Flow<List<LLDocument>>,
    covers: Map<String, String?>,
    isDocumentInBookList: (id: String) -> Flow<Boolean>,
    onItemClick: (document: LLDocument) -> Unit,
    onAddToFavoritesClicked: (document: LLDocument) -> Unit,
    onMarkAsReadClicked: (document: LLDocument) -> Unit,
    onAddToBookList: (document: LLDocument) -> Unit,
    onRemoveFromBookList: (documentId: String) -> Unit,
    onToggleAutoTracking: (document: LLDocument) -> Unit,
    onDeleteDocument: (document: LLDocument) -> Unit,
    onShare: (document: LLDocument) -> Unit
) {
    if (visible) {
        var query by remember { mutableStateOf("") }
        var searchResults: Flow<List<LLDocument>> by remember { mutableStateOf(emptyFlow()) }
        val items by searchResults.collectAsState(emptyList())

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
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
                                searchResults = onSearch(query)
                            },
                            onSearch = {
                                searchResults = onSearch(it)
                            },
                            expanded = true,
                            onExpandedChange = { },
                            modifier = Modifier,
                            enabled = true,
                            placeholder = { Text(stringResource(LLString.searchPlaceHolder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (query.isBlank()) {
                                            onDismiss()
                                        } else {
                                            query = ""
                                        }
                                    },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        )
                    },
                    expanded = true,
                    onExpandedChange = { }
                ) {
                    Card(modifier = Modifier.padding(4.dp)) {
                        LazyColumn {
                            items(items) { document ->
                                val inBookList by isDocumentInBookList(document.id).collectAsState(
                                    false
                                )

                                ReaderListItem(
                                    document = document,
                                    documentInBookList = inBookList,
                                    cover = covers[document.cover],
                                    onClick = { onItemClick(document) },
                                    onLongClick = { },
                                    onAddToFavoritesClicked = { onAddToFavoritesClicked(document) },
                                    onMarkAsReadClicked = { onMarkAsReadClicked(document) },
                                    onAddToBookList = { onAddToBookList(document) },
                                    onRemoveFromBookList = { onRemoveFromBookList(document.id) },
                                    onToggleAutoTracking = { onToggleAutoTracking(document) },
                                    onDeleteDocument = { onDeleteDocument(document) },
                                    onShare = { onShare(document) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
