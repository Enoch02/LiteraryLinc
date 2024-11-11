package com.enoch02.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.enoch02.database.model.ReaderFilter
import com.enoch02.reader.R

@Composable
fun NoDocumentView(
    modifier: Modifier = Modifier,
    filter: ReaderFilter,
    onScanForDocs: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            when (filter) {
                ReaderFilter.READING -> {
                    Text(text = stringResource(R.string.nothing_to_continue))
                }

                ReaderFilter.FAVORITES -> {
                    Text(text = stringResource(R.string.no_favorite_docs))
                }

                ReaderFilter.COMPLETED -> {
                    Text(text = stringResource(R.string.no_completed_docs))
                }

                ReaderFilter.ALL -> {
                    Text(text = stringResource(R.string.no_doc_found))
                    Button(
                        onClick = onScanForDocs,
                        content = {
                            Text(text = stringResource(R.string.scan_for_new_docs))
                        }
                    )
                }

                ReaderFilter.NO_FILE -> {
                    Text(text = stringResource(R.string.no_doc_found))
                }
            }
        }
    )
}