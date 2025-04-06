package com.enoch02.stats.stats

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.resources.LLString
import com.enoch02.stats.components.QuickStatCard

@Composable
fun StatsScreen(
    modifier: Modifier,
    viewModel: StatsScreenViewModel = hiltViewModel()
) {
    val currentStreak by viewModel.currentReadingStreak.collectAsState(0)
    val longestStreak by viewModel.longestReadingStreak.collectAsState(0)

    val formattedStreakMessage = remember(currentStreak) {
        viewModel.formatCurrentStreakMessage(currentStreak)
    }
    val formattedLongestStreakMessage = remember(longestStreak) {
        viewModel.formatLongestStreakMessage(longestStreak)
    }

    val readingGoal by viewModel.readingGoal.collectAsState(0)
    val readingProgress by viewModel.readingProgress.collectAsState(0)
    var timeRemainingForStreak by remember { mutableStateOf("") }

    LaunchedEffect(currentStreak) {
        timeRemainingForStreak = viewModel.getFormattedTimeRemainingForStreak()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            StreakView(
                currentStreak = currentStreak,
                streakMessage = formattedStreakMessage,
                timeRemaining = timeRemainingForStreak
            )

            Spacer(Modifier.height(6.dp))

            ReadingProgressView(
                modifier = Modifier,
                progress = readingProgress,
                goal = readingGoal
            )

            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .padding(bottom = 4.dp)
            ) {
                item {
                    QuickStatCard(
                        title = stringResource(LLString.totalBooksRead),
                        value = viewModel.totalCount.withCommas(),
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(LLString.pagesRead),
                        value = viewModel.pagesReadCount.withCommas(),
                        icon = Icons.AutoMirrored.Rounded.MenuBook
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(LLString.totalHours),
                        value = viewModel.totalHoursRead.withCommas(),
                        icon = Icons.Rounded.AccessTime
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(LLString.fastestBookCompleted),
                        value = viewModel.fastestCompletedBook,
                        icon = Icons.Rounded.Timelapse
                    )
                }

                item {
                    QuickStatCard(
                        title = "Reading",
                        value = stringResource(
                            LLString.currentlyReading,
                            viewModel.currentlyReadingCount
                        ),
                        icon = Icons.AutoMirrored.Rounded.ReadMore
                    )
                }

                item {
                    QuickStatCard(
                        title = "Longest Streak",
                        value = formattedLongestStreakMessage,
                        icon = Icons.Rounded.CalendarMonth
                    )
                }

                item {
                    QuickStatCard(
                        title = stringResource(LLString.completedThisYear),
                        value = viewModel.booksReadThisYear.withCommas(),
                        icon = Icons.Rounded.Check
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(LLString.roughEstimate),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    )
}

@Composable
fun StreakView(
    modifier: Modifier = Modifier,
    currentStreak: Int,
    streakMessage: String,
    timeRemaining: String
) {
    Column(modifier = modifier) {
        Text(
            text = streakMessage,
            style = MaterialTheme.typography.headlineSmall
        )

        if (currentStreak >= 1) {
            Text(
                text = timeRemaining
            )
        }
    }
}

@Composable
fun ReadingProgressView(modifier: Modifier, progress: Int, goal: Int) {
    val message = if (progress >= 0 && goal > 0) {
        stringResource(LLString.readingGoalText, progress, goal)
    } else {
        stringResource(LLString.setReadingGoalMsg)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(message)

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = {
                if (progress > 0 && goal > 0) {
                    progress.toFloat() / goal
                } else {
                    0f
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}