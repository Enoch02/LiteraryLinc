package com.enoch02.stats.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLIcons


@Composable
fun LeaderBoardView(
    modifier: Modifier = Modifier,
    header: String,
    values: List<LeaderBoardValue>
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(text = header, style = MaterialTheme.typography.labelLarge)

        Card {
            LazyColumn {
                items(values.size) { index ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                painterResource(LLIcons.leaderBoardIcons[index]),
                                contentDescription = "Rank ${index + 1}"
                            )
                        },
                        headlineContent = { Text(values[index].name) },
                        trailingContent = { Text(values[index].count) }
                    )

                    if (index != 4) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

data class LeaderBoardValue(
    val name: String,
    val count: String
)