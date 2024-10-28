package com.artifex.mupdf.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.artifex.mupdf.viewer.old.Pallet
import com.artifex.mupdf.viewer.shared.Item
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState

class LLOutlineActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var selected by mutableIntStateOf(-1)
        val outline: SnapshotStateList<Item> = mutableStateListOf()

        val idx = intent.getIntExtra("PALLETBUNDLE", -1)
        val bundle = Pallet.receiveBundle(idx)

        if (bundle != null) {
            val currentPage = bundle.getInt("POSITION")

            @Suppress("DEPRECATION")
            val items = bundle.getSerializable("OUTLINE") as? List<Item>

            items?.let {
                outline.addAll(it)
                selected = it.indexOfFirst { item -> item.page >= currentPage }
                    .takeIf { it1 -> it1 != -1 } ?: 0 // Default to 0 if not found
            }
        }

        setContent {
            val listState = rememberLazyListState()
            val state = rememberScrollAreaState(listState)

            LaunchedEffect(
                key1 = Unit,
                block = {
                    val offset = 5

                    if (selected - offset > 0 && selected < outline.size) {
                        listState.animateScrollToItem(selected - 5)
                    }
                }
            )

            ScrollArea(
                state = state,
                content = {
                    LazyColumn(
                        state = listState,
                        content = {
                            items(
                                count = outline.size,
                                itemContent = { index ->
                                    val item = outline[index]
                                    val backgroundColor = if (index == selected) {
                                        Color.Blue.copy(alpha = 0.3f) // Tint for selected item
                                    } else {
                                        Color.Transparent // Default background
                                    }

                                    ListItem(
                                        headlineContent = { Text(text = item.title) },
                                        trailingContent = { Text(text = "${item.page}") },
                                        colors = ListItemDefaults.colors(containerColor = backgroundColor),
                                        modifier = Modifier
                                            .clickable {
                                                onClickListItem(item)
                                            }
                                    )
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxHeight()
                            .width(8.dp),
                        thumb = {
                            Thumb(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray)
                            )
                        }
                    )
                }
            )
        }
    }

    private fun onClickListItem(item: Item) {
        setResult(RESULT_FIRST_USER + item.page)
        finish()
    }
}
