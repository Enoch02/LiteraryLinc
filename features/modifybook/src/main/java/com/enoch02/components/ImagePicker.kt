package com.enoch02.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.addbook.R

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
            type = "image/*"
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
                        contentDescription = stringResource(R.string.add_image),
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

/***
 * Modified version of [ImagePicker] that uses
 * uri and a path to determine what image to display
 */
@Composable
internal fun ImagePicker(
    label: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    coverImageUri: Uri?,
    coverImagePath: String?
) {
    val intent = Intent(
        Intent.ACTION_OPEN_DOCUMENT,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    )
        .apply {
            type = "image/*"
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
                if (coverImageUri == null && coverImagePath.isNullOrEmpty()) {
                    Icon(
                        painter = painterResource(R.drawable.round_image_24),
                        contentDescription = stringResource(R.string.add_image_desc),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = coverImageUri ?: coverImagePath,
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
