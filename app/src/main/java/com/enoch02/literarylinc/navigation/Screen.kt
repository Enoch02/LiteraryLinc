package com.enoch02.literarylinc.navigation

enum class TopLevelDestination {
    BOOK_LIST,
    SEARCH,
    STATS,
    MORE
}

sealed class Screen(val route: String) {

    object LiteraryLincApp : Screen("literarylinc_app")
    object AddBook : Screen("add_book_screen")
    object EditBook : Screen("edit_book_screen")
    object BookDetail : Screen("book_detail_screen")
    object BarcodeScanner : Screen("barcode_scanner_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
