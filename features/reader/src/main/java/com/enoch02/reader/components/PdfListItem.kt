package com.enoch02.reader.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PdfListItem(
    name: String,
    thumbnail: Bitmap?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(name)
        },
        leadingContent = {
            thumbnail?.let {
                Image(
                    it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(width = 50.dp, height = 80.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        },
        modifier = modifier.clickable {
            onClick()
        }
    )
}

@Preview
@Composable
private fun Preview() {
    PdfListItem(name = "Hello", thumbnail = null, onClick = {})
}