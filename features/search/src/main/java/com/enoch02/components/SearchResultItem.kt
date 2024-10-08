package com.enoch02.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.search.R

@Composable
fun SearchResultItem(
    title: String,
    author: List<String>,
    coverUrl: String,
    itemInDatabase: Boolean,
    onClick: () -> Unit,
    onAddBtnClick: () -> Unit,
    onEditBtnClick: () -> Unit
) {
    var state by rememberSaveable {
        mutableStateOf(ItemAddState.NOT_LOADING)
    }

    // triggers when addBtnClick runs successfully
    if (itemInDatabase) {
        state = ItemAddState.NOT_LOADING
    }

    ListItem(
        headlineContent = {
            Text(
                text = title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
            if (author.isNotEmpty()) {
                Text(
                    text = "by ${author.first()}",
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        },
        leadingContent = {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(width = 50.dp, height = 80.dp)
            )
        },
        trailingContent = {
            when (state) {
                ItemAddState.NOT_LOADING -> {
                    OutlinedIconButton(
                        onClick = {
                            if (itemInDatabase) {
                                onEditBtnClick()
                            } else {
                                state = ItemAddState.LOADING
                                onAddBtnClick()
                            }
                        },
                        shape = RectangleShape,
                        content = {
                            Icon(
                                painter = if (itemInDatabase) {
                                    painterResource(id = R.drawable.round_edit_24)
                                } else {
                                    painterResource(id = R.drawable.round_add_24)
                                },
                                contentDescription = null
                            )
                        }
                    )
                }

                ItemAddState.LOADING -> {
                    CircularProgressIndicator()
                }
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}