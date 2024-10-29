package com.enoch02.more.navigation

sealed class MoreScreenDestination(val route: String) {

    data object More : MoreScreenDestination("more_screen")
    data object PomoTimer : MoreScreenDestination("pomo_timer_screen")
    data object CustomTags : MoreScreenDestination("custom_tags_screen")
    data object Wishlist : MoreScreenDestination("wishlist_screen")
    data object Settings : MoreScreenDestination("settings_screen")
    data object BackupRestore : MoreScreenDestination("backup_restore_screen")
    data object About : MoreScreenDestination("about_app_screen")
    data object Scanner: MoreScreenDestination("file_scanner_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}