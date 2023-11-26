package com.enoch02.more.navigation

sealed class MoreScreenDestination(val route: String) {

    object More : MoreScreenDestination("more_screen")
    object PomoTimer : MoreScreenDestination("pomo_timer_screen")
    object CustomTags : MoreScreenDestination("custom_tags_screen")
    object Wishlist : MoreScreenDestination("wishlist_screen")
    object Settings : MoreScreenDestination("settings_screen")
    object BackupRestore : MoreScreenDestination("backup_restore_screen")
    object Update : MoreScreenDestination("check_update_screen")
    object About : MoreScreenDestination("about_app_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}