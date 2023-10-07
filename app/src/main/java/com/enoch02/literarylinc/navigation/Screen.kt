package com.enoch02.literarylinc.navigation

enum class TopLevelDestination {
    BOOK_LIST,
    SEARCH,
    STATS,
    MORE
}

sealed class Screen(val route: String) {

    object MainScaffold : Screen("main_scaffold")
    object AddBookScreen : Screen("add_book_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
