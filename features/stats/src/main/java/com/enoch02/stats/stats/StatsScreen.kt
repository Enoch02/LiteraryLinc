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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.stats.R
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
                        title = stringResource(R.string.total_books_read),
                        value = viewModel.totalCount.withCommas(),
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(R.string.pages_read),
                        value = viewModel.pagesReadCount.withCommas(),
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(R.string.total_hours),
                        value = viewModel.totalHoursRead.withCommas(),
                        icon = Icons.Rounded.AccessTime
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(R.string.fastest_book_completed),
                        value = viewModel.fastestCompletedBook,
                        icon = Icons.Rounded.Timelapse
                    )
                }

                item {
                    QuickStatCard(
                        title = "Reading",
                        value = stringResource(
                            R.string.currently_reading,
                            viewModel.currentlyReadingCount
                        ),
                        icon = Icons.AutoMirrored.Rounded.ReadMore
                    )
                }

                if (0 != context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
                    item {
                        QuickStatCard(
                            title = "Longest Streak",
                            value = "n Days",
                            icon = Icons.Rounded.CalendarMonth
                        )
                    }
                }

                item {
                    QuickStatCard(
                        title = stringResource(R.string.completed_this_year),
                        value = viewModel.booksReadThisYear.withCommas(),
                        icon = Icons.Rounded.Check
                    )
                }
            }

            Text(
                text = stringResource(R.string.rough_estimate),
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}

@Composable
fun ReadingProgressView(modifier: Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Yearly Goal: 15/30 Books")

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { 0.5f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}