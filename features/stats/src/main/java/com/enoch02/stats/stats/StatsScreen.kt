package com.enoch02.stats.stats

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReadMore
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.stats.components.LeaderBoardValue
import com.enoch02.stats.components.LeaderBoardView
import com.enoch02.stats.components.QuickStatCard

@Composable
fun StatsScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: StatsScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                Column(modifier = Modifier.weight(0.1f)) {
                    Text(
                        text = "🔥 10-Day Reading Streak",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    ReadingProgressView(modifier = Modifier)
                }
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
                        value = "${viewModel.totalCount}",
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = "Pages Read",
                        value = "${viewModel.pagesReadCount}",
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = "Total Hours*",
                        value = "${viewModel.totalHoursRead}",
                        icon = Icons.Rounded.AccessTime
                    )
                }

                item {
                    QuickStatCard(
                        title = "Fastest Book Completed",
                        value = viewModel.fastestCompletedBook,
                        icon = Icons.Rounded.Timelapse
                    )
                }

                item {
                    QuickStatCard(
                        title = "Reading",
                        value = "${viewModel.currentlyReadingCount} Books",
                        icon = Icons.AutoMirrored.Rounded.ReadMore
                    )
                }

                item {
                    QuickStatCard(
                        title = "Longest Streak",
                        value = "n Days",
                        icon = Icons.Rounded.CalendarMonth
                    )
                }
            }

            //TODO: show multiple leaderboards using a pager
            //TODO: might not use this
            /*LeaderBoardView(
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
            )*/
        }
    )
}

@Composable
fun ReadingProgressView(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Yearly Goal: 15/30 Books")

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}