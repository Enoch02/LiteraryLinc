package com.enoch02.stats.stats

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.resources.LLIcons
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    navController: NavController,
    modifier: Modifier,
    statsScreenViewModel: StatsScreenViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit, block = { statsScreenViewModel.get() })
    ReadingStatsDashboard(modifier = modifier, totalBooks = statsScreenViewModel.total.intValue)
}

@Composable
fun ReadingStatsDashboard(modifier: Modifier, totalBooks: Int) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            Column(modifier = Modifier.weight(0.1f)) {
                Text(
                    text = "🔥 10-Day Reading Streak",
                    style = MaterialTheme.typography.headlineSmall
                )
                ReadingProgress(modifier = Modifier)
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .weight(0.45f)
                    .padding(bottom = 4.dp)
            ) {
                item {
                    QuickStatCard(
                        title = "Total Books Read",
                        value = "$totalBooks",
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = "Pages Read",
                        value = "5,630",
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = "Total Hours",
                        value = "27h",
                        icon = Icons.Rounded.AccessTime
                    )
                }

                item {
                    QuickStatCard(
                        title = "Fastest Book Completed",
                        value = "Rascal Does Not Dream of Bunny Girl Senpai",
                        icon = Icons.Rounded.Timelapse
                    )
                }

                item {
                    QuickStatCard(
                        title = "Reading",
                        value = "3 Books",
                        icon = Icons.AutoMirrored.Rounded.ReadMore
                    )
                }

                item {
                    QuickStatCard(
                        title = "Placeholder",
                        value = "IDK",
                        icon = Icons.Rounded.QuestionMark
                    )
                }
            }

            //TODO: show multiple leaderboards using a pager
            LeaderBoardView(
                modifier = Modifier.weight(0.45f),
                header = "Most Read Book Types",
                values = listOf(
                    LeaderBoardValue(
                        name = "Book 1",
                        count = "10"
                    ),
                    LeaderBoardValue(
                        name = "Book 2",
                        count = "5"
                    ),
                    LeaderBoardValue(
                        name = "Book 3",
                        count = "4"
                    ),
                    LeaderBoardValue(
                        name = "Book 4",
                        count = "2"
                    ),
                    LeaderBoardValue(
                        name = "Book 5",
                        count = "1"
                    )
                )
            )
        }
    )
}

@Composable
fun ReadingProgress(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Yearly Goal: 15/30 Books")

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun QuickStatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .height(100.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = title)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal
                )
            }

            AutoScrollingText(value, modifier = Modifier.fillMaxWidth())
        }
    }
}

//TODO: move to a common module in case i need to reuse it
@Composable
fun AutoScrollingText(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val textLayoutInfo = remember { mutableStateOf<TextLayoutInfo?>(null) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .horizontalScroll(scrollState)
            .onGloballyPositioned { coordinates ->
                textLayoutInfo.value = TextLayoutInfo(
                    width = with(density) { coordinates.size.width.toDp() },
                    lineCount = 1
                )
            }
            .padding(horizontal = 16.dp),
        maxLines = 1,
        softWrap = false
    )

    LaunchedEffect(textLayoutInfo.value) {
        textLayoutInfo.value?.let { layoutInfo ->
            if (layoutInfo.width > 100.dp) { // Adjust threshold as needed
                coroutineScope.launch {
                    while (true) {
                        scrollState.animateScrollTo(
                            value = scrollState.maxValue,
                            animationSpec = tween(
                                durationMillis = scrollState.maxValue * 15, // Increased multiplier for slower scrolling
                                easing = LinearEasing
                            )
                        )
                        delay(1500) // Longer pause at the end
                        scrollState.animateScrollTo(
                            value = 0,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = LinearEasing
                            )
                        )
                        delay(1500) //
                    }
                }
            }
        }
    }
}

data class TextLayoutInfo(
    val width: Dp,
    val lineCount: Int
)

//TODO
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    /*StatsScreenContent(
        modifier = Modifier,
        total = 100,
        completed = 69,
        categoriesStats = CategoriesStats()
    )*/
    ReadingStatsDashboard(modifier = Modifier, totalBooks = 100)
}