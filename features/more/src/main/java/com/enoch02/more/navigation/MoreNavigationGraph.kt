package com.enoch02.more.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.enoch02.more.MoreScreen

@Composable
fun MoreNavigationGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = MoreScreen.More.route,
        builder = {
            composable(route = MoreScreen.More.route) {
                MoreScreen(modifier = Modifier)
            }

            composable(route = MoreScreen.PomoTimer.route) {

            }

            composable(route = MoreScreen.CustomTags.route) {

            }

            composable(route = MoreScreen.Wishlist.route) {

            }

            composable(route = MoreScreen.Settings.route) {

            }

            composable(route = MoreScreen.BackupRestore.route) {

            }

            composable(route = MoreScreen.Update.route) {

            }

            composable(route = MoreScreen.About.route) {

            }
        }
    )
}