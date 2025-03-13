package com.enoch02.resources

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.enoch02.resources.workers.CHANNEL_ID
import com.enoch02.resources.workers.NOTIFICATION_ID
import com.enoch02.resources.workers.NOTIFICATION_TITLE
import com.enoch02.resources.workers.COMPLETION_NOTIFICATION_CHANNEL_DESCRIPTION
import com.enoch02.resources.workers.COMPLETION_NOTIFICATION_CHANNEL_NAME
import com.enoch02.resources.workers.PROGRESS_CHANNEL_ID
import com.enoch02.resources.workers.PROGRESS_NOTIFICATION_CHANNEL_DESCRIPTION
import com.enoch02.resources.workers.PROGRESS_NOTIFICATION_CHANNEL_NAME
import com.enoch02.resources.workers.PROGRESS_NOTIFICATION_ID

fun makeStatusNotification(message: String, context: Context) {

    // Make a channel if necessary
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = COMPLETION_NOTIFICATION_CHANNEL_NAME
        val description = COMPLETION_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        // Add the channel
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        notificationManager?.createNotificationChannel(channel)
    }

    // Create the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.app_icon_svg)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVibrate(LongArray(0))

    // Show the notification
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Toast.makeText(
            context,
            "Allow notification permission in settings to get notified when the scan completes",
            Toast.LENGTH_SHORT
        ).show()
        return
    }
    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
}

fun createIndeterminateProgressNotification(
    context: Context,
    title: String,
    message: String = "Please wait..."
) {
    val builder = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.app_icon_svg)
        .setProgress(0, 0, true)
        .setOngoing(true)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    NotificationManagerCompat.from(context).notify(PROGRESS_NOTIFICATION_ID, builder.build())
}

fun createProgressNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = PROGRESS_CHANNEL_ID
        val channelName = PROGRESS_NOTIFICATION_CHANNEL_NAME
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = PROGRESS_NOTIFICATION_CHANNEL_DESCRIPTION
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}

fun sendFinalProgressNotification(context: Context) {
    val finalNotification = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText("Loading Complete!")
        .setSmallIcon(R.drawable.app_icon_svg)
        .setOngoing(false)
        .build()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    NotificationManagerCompat.from(context).notify(PROGRESS_NOTIFICATION_ID, finalNotification)
}