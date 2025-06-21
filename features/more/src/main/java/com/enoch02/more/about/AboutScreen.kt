package com.enoch02.more.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enoch02.more.navigation.MoreScreenDestination
import com.enoch02.resources.LLString
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val clipBoard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(LLString.about)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(LLString.navigateBack)
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
                        val appVersionText = "${version.first}(${version.second})"

                        ListItem(
                            headlineContent = {
                                Card {
                                    ListItem(
                                        headlineContent = { Text(text = stringResource(LLString.version)) },
                                        supportingContent = {
                                            Text(text = appVersionText)
                                        },
                                        modifier = Modifier
                                            .alpha(alpha)
                                            .clickable {
                                                clipBoard.setText(
                                                    buildAnnotatedString {
                                                        append(getDeviceInfo(context))
                                                    }
                                                )
                                            },
                                        tonalElevation = 30.dp
                                    )

                                    ListItem(
                                        headlineContent = {
                                            Text(text = stringResource(LLString.checkForUpdatesLabel))
                                        },
                                        supportingContent = {
                                            Text(stringResource(LLString.checkUpdatesDesc))
                                        },
                                        modifier = Modifier.clickable {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    context.getString(LLString.appRepoUrl).toUri()
                                                )
                                            )
                                        },
                                        tonalElevation = 30.dp
                                    )

                                    ListItem(
                                        headlineContent = {
                                            Text(text = stringResource(LLString.ossLicensesLabel))
                                        },
                                        supportingContent = {
                                            Text(stringResource(LLString.ossLicensesDesc))
                                        },
                                        modifier = Modifier.clickable {
                                            navController.navigate(MoreScreenDestination.Licenses.route)
                                        },
                                        tonalElevation = 30.dp
                                    )

                                    ListItem(
                                        headlineContent = {
                                            Text(text = stringResource(LLString.visitRepoLabel))
                                        },
                                        supportingContent = {
                                            Text(stringResource(LLString.visitRepoDesc))
                                        },
                                        modifier = Modifier.clickable {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    context.getString(LLString.appRepoUrl).toUri()
                                                )
                                            )
                                        },
                                        tonalElevation = 30.dp
                                    )
                                }
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
        versionName = packageInfo.versionName.toString()
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

private fun getDeviceInfo(context: Context): String {
    var versionName = ""
    var versionCode = 0
    val androidVersion = Build.VERSION.RELEASE
    val sdkVersion = Build.VERSION.SDK_INT
    val deviceBrand = Build.BRAND
    val deviceManufacturer = Build.MANUFACTURER
    val deviceName = Build.DEVICE
    val deviceModel = Build.MODEL

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
        versionName = packageInfo.versionName.toString()
        versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                packageInfo.versionCode
            }
    } catch (e: Exception) {
        Log.d("getDeviceInfo", "getDeviceInfo: ${e.message}")
    }

    return """
        App Version Name: $versionName
        App Version Code: $versionCode
        Android Version: $androidVersion
        SDK Version: $sdkVersion
        Device Brand: $deviceBrand
        Device Manufacturer: $deviceManufacturer
        Device Name: $deviceName
        Device Model: $deviceModel
    """.trimIndent()
}
