package com.enoch02.more.file_scan.util

import android.net.Uri
import java.util.Locale

fun getFileExtension(uri: Uri): String {
    return uri.path?.substringAfterLast('.', "")?.lowercase(Locale.ROOT) ?: ""
}