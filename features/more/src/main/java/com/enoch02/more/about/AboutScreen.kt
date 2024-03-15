package com.enoch02.more.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.enoch02.more.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.about_label)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.back_label)
                            )
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                content = {
                    item {
                        val version = getAppVersion(LocalContext.current)
                        val alpha by rememberSaveable {
                            mutableFloatStateOf(
                                if (version.first == "" || version.second == 0) 0f else 1f
                            )
                        }

                        ListItem(
                            headlineContent = { Text(text = stringResource(R.string.version_label)) },
                            supportingContent = {
                                Text(text = "version ${version.first}(${version.second})")
                            },
                            modifier = Modifier
                                .alpha(alpha)
                                .clickable {
                                    /*TODO: open the repo releases page?*/
                                }
                        )
                    }

                    item {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(R.string.check_for_updates_label))
                            },
                            modifier = Modifier.clickable {
                                /*TODO*/
                            }
                        )
                    }

                    item {
                        ListItem(
                            headlineContent = {
                                Text(text = stringResource(R.string.open_source_licenses_label))
                            },
                            modifier = Modifier.clickable {
                                /*TODO*/
                            }
                        )
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}

private fun getAppVersion(context: Context): Pair<String, Int> {
    var versionName = ""
    var versionCode = 0

    try {
        val packageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        versionName = packageInfo.versionName
        versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
    } catch (e: Exception) {
        Log.d("getAppVersion", "getAppVersion: ${e.message}")
    }

    return Pair(versionName, versionCode)
}
