package com.enoch02.more.navigation

sealed class MoreScreen(val route: String) {

    object More : MoreScreen("more_screen")
    object PomoTimer : MoreScreen("pomo_timer_screen")
    object CustomTags : MoreScreen("custom_tags_screen")
    object Wishlist : MoreScreen("wishlist_screen")
    object Settings : MoreScreen("settings_screen")
    object BackupRestore : MoreScreen("backup_restore_screen")
    object Update : MoreScreen("check_update_screen")
    object About : MoreScreen("about_app_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}