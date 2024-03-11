package com.enoch02.stats.stats

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun StatsScreen(
    navController: NavController,
    modifier: Modifier,
    statsScreenViewModel: StatsScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit, block = { statsScreenViewModel.get() })

    StatsScreenContent(
        modifier = modifier,
        total = statsScreenViewModel.total.value,
        completed = statsScreenViewModel.completed.value
    )
}

@Composable
private fun StatsScreenContent(modifier: Modifier, total: Int, completed: Int) {
    LazyColumn(
        content = {
            item {
                StatHeadingText(value = "Overview")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        StatItem(
                            label = "Book${if (total > 1) "s" else ""} in Library",
                            value = "$total"
                        )
                        StatItem(
                            label = "Book${if (completed > 1) "s" else ""} Completed",
                            value = "$completed"
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                StatHeadingText(value = "Categories")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        StatItem(label = "Manga", value = "0")
                        StatItem(label = "Light Novel", value = "0")
                        StatItem(label = "Comic", value = "0")
                        StatItem(label = "Novel", value = "0")
                        StatItem(label = "Any Type", value = "0")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    )
}

@Composable
fun StatHeadingText(value: String) {
    Text(
        text = value,
        Modifier.padding(bottom = 16.dp),
        fontSize = MaterialTheme.typography.titleLarge.fontSize
    )
}

@Composable
fun StatItem(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append(value)
            append(" ")
            pop()
            append(label)
        },
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    StatsScreenContent(modifier = Modifier, total = 100, completed = 69)
}