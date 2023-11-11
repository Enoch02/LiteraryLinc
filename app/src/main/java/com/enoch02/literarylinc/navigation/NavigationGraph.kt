package com.enoch02.literarylinc.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.enoch02.literarylinc.ui.LiteraryLincApp
import com.enoch02.addbook.AddBookScreen
import com.enoch02.barcodescanner.BarcodeScannerScreen
import com.enoch02.bookdetail.BookDetailScreen

@Composable
fun NavigationGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.LiteraryLincApp.route,
        builder = {
            composable(Screen.LiteraryLincApp.route) {
                LiteraryLincApp(navController = navController)
            }

            composable(Screen.AddBook.route) {
                AddBookScreen(
                    navController = navController,
                    modifier = Modifier.padding(8.dp)
                )
            }

            composable(
                route = Screen.BookDetail.route + "/{id}",
                arguments = listOf(navArgument(name = "id") { type = NavType.IntType })
            ) { entry ->
                entry.arguments?.getInt("id")?.let { id ->
                    BookDetailScreen(navController = navController, id = id)
                }
            }

            composable(Screen.BarcodeScanner.route) {
                BarcodeScannerScreen(navController = navController)
            }
        }
    )
}
