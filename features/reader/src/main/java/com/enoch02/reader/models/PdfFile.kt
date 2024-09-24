package com.enoch02.reader.models

import android.graphics.Bitmap
import android.net.Uri

//TODO: rename
data class PdfFile(
    val contentUri: Uri,
    val name: String,
    val thumbnail: Bitmap?
)