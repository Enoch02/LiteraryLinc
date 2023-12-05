package com.enoch02.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.enoch02.addbook.R

@Composable
internal fun BackArrowButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        content = {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back_desc)
            )
        }
    )
}