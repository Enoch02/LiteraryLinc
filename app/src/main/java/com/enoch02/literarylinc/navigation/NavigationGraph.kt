package com.enoch02.literarylinc.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enoch02.literarylinc.ui.LiteraryLincApp
import com.enoch02.addbook.AddBookScreen

@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScaffold.route,
        builder = {
            composable(Screen.MainScaffold.route) {
                LiteraryLincApp(navController = navController)
            }

            composable(Screen.AddBookScreen.route) {
                AddBookScreen(
                    navController = navController,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    )
}