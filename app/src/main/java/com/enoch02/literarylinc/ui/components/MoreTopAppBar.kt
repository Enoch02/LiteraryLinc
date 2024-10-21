package com.enoch02.literarylinc.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.literarylinc.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTopAppBar() {
    TopAppBar(
        title = {
            Text(text = stringResource(R.string.more_label))
        }
    )
}