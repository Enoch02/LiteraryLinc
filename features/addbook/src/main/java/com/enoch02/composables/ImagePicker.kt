package com.enoch02.composables

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.addbook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImagePicker(
    label: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    coverImageUri: Uri?
) {
    val intent = Intent(
        Intent.ACTION_OPEN_DOCUMENT,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    )
        .apply {
            type = "image/jpeg"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

    Column {
        Text(text = label, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            onClick = {
                launcher.launch(intent)
            },
            content = {
                if (coverImageUri == null) {
                    Icon(
                        painter = painterResource(R.drawable.round_image_24),
                        contentDescription = "Add Image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = coverImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            modifier = Modifier.size(120.dp, 180.dp)
        )
    }
}