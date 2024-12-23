package com.enoch02.literarylinc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.enoch02.bookdetail.BookDetailScreen
import com.enoch02.literarylinc.ui.LiteraryLincApp
import com.enoch02.modifybook.AddBookScreen
import com.enoch02.modifybook.EditBookScreen
import com.enoch02.more.MoreScreen
import com.enoch02.more.about.AboutScreen
import com.enoch02.more.about.LicensesScreen
import com.enoch02.more.backup_restore.BackupRestoreScreen
import com.enoch02.more.file_scan.FileScanScreen
import com.enoch02.more.navigation.MoreScreenDestination
import com.enoch02.more.settings.SettingsScreen

@Composable
fun LiteraryLincNavHost(navController: NavHostController = rememberNavController()) {
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
                    modifier = Modifier
                )
            }

            composable(
                route = Screen.EditBook.route + "/{id}",
                arguments = listOf(navArgument(name = "id") { type = NavType.IntType })
            ) { entry ->
                entry.arguments?.getInt("id")?.let { id ->
                    EditBookScreen(navController = navController, id = id)
                }
            }

            composable(
                route = Screen.BookDetail.route + "/{id}",
                arguments = listOf(navArgument(name = "id") { type = NavType.IntType })
            ) { entry ->
                entry.arguments?.getInt("id")?.let { id ->
                    BookDetailScreen(
                        navController = navController,
                        id = id,
                        editScreenRoute = {
                            Screen.EditBook.withArgs(id.toString())
                        }
                    )
                }
            }

            // nested destinations from MoreScreen
            navigation(startDestination = MoreScreenDestination.More.route, route = "more_stuff") {
                composable(route = MoreScreenDestination.More.route) {
                    MoreScreen(navController = navController, modifier = Modifier)
                }

                composable(route = MoreScreenDestination.PomoTimer.route) {

                }

                composable(route = MoreScreenDestination.CustomTags.route) {

                }

                composable(route = MoreScreenDestination.Wishlist.route) {

                }

                composable(route = MoreScreenDestination.Settings.route) {
                    SettingsScreen(navController = navController)
                }

                composable(route = MoreScreenDestination.BackupRestore.route) {
                    BackupRestoreScreen(navController = navController)
                }

                composable(route = MoreScreenDestination.About.route) {
                    AboutScreen(navController = navController)
                }

                composable(route = MoreScreenDestination.Scanner.route) {
                    FileScanScreen(navController = navController)
                }

                composable(route = MoreScreenDestination.Licenses.route) {
                    LicensesScreen(navController = navController)
                }
            }
        }
    )
}
