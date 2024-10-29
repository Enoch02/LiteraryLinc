package com.enoch02.literarylinc.navigation

enum class TopLevelDestination {
    BOOK_LIST,
    READER,
    STATS,
    MORE
}

sealed class Screen(val route: String) {

    data object LiteraryLincApp : Screen("literarylinc_app")
    data object AddBook : Screen("add_book_screen")
    data object EditBook : Screen("edit_book_screen")
    data object BookDetail : Screen("book_detail_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
