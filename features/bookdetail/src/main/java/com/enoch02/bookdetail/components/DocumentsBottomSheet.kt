package com.enoch02.bookdetail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.database.model.LLDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsBottomSheet(
    documents: List<LLDocument>,
    covers: Map<String, String?>,
    onDismiss: () -> Unit,
    onDocumentSelected: (documentId: String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        content = {
            Card(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                content = {
                    LazyColumn(
                        content = {
                            items(
                                items = documents,
                                itemContent = { document ->
                                    ListItem(
                                        leadingContent = {
                                            AsyncImage(
                                                model = covers[document.cover],
                                                contentDescription = null,
                                                modifier = Modifier.size(50.dp)
                                            )
                                        },
                                        headlineContent = {
                                            Text(text = document.name)
                                        },
                                        modifier = Modifier.clickable {
                                            onDocumentSelected(document.id)
                                        }
                                    )

                                    if (documents.indexOf(document) != documents.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            )
                        }
                    )
                }
            )
        }
    )
}