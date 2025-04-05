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
import com.enoch02.resources.LLString

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
                    Text(text = stringResource(LLString.nothingToContinue))
                }

                ReaderFilter.FAVORITES -> {
                    Text(text = stringResource(LLString.notFavoritesDocs))
                }

                ReaderFilter.COMPLETED -> {
                    Text(text = stringResource(LLString.noCompletedDocs))
                }

                ReaderFilter.ALL -> {
                    Text(text = stringResource(LLString.noDocFound))
                    Button(
                        onClick = onScanForDocs,
                        content = {
                            Text(text = stringResource(LLString.scanForNewDocs))
                        }
                    )
                }

                ReaderFilter.NO_FILE -> {
                    Text(text = stringResource(LLString.noDocFound))
                }
            }
        }
    )
}