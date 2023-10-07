package com.enoch02.literarylinc.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.enoch02.booklist.BookListScreen
import com.enoch02.literarylinc.R
import com.enoch02.literarylinc.navigation.Screen
import com.enoch02.literarylinc.navigation.TopLevelDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteraryLincApp(navController: NavController) {
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TopLevelDestination.BOOK_LIST) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(id = R.string.app_name)) })
        },
        bottomBar = {
            val screens = TopLevelDestination.values()
            val icons = listOf(
                Icons.Rounded.ListAlt,
                Icons.Rounded.Search,
                Icons.Rounded.Analytics,
                Icons.Rounded.MoreHoriz
            )
            val labels = listOf(
                stringResource(R.string.book_list_label),
                stringResource(R.string.search_label),
                stringResource(R.string.statistics_label),
                stringResource(R.string.more_label)
            )

            NavigationBar {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = screen == currentScreen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(imageVector = icons[index], contentDescription = null)
                        },
                        label = {
                            Text(
                                text = labels[index],
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddBookScreen.route) },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add_new_book_desc)
                    )
                },
            )
        },
        content = { paddingValues ->
            Crossfade(
                targetState = currentScreen,
                content = {
                    when (it) {
                        TopLevelDestination.BOOK_LIST -> {
                            BookListScreen(
                                modifier = Modifier.padding(paddingValues),
                                scope = scope
                            )
                        }

                        TopLevelDestination.SEARCH -> {

                        }

                        TopLevelDestination.STATS -> {

                        }

                        TopLevelDestination.MORE -> {
                            //TODO: IMPORT!!
                            /*MoreScreen(modifier = Modifier.padding(paddingValues))*/
                        }
                    }
                },
                label = "MainScaffold Cross-fade"
            )
        }
    )
}