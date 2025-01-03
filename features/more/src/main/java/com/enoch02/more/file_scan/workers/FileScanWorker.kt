package com.enoch02.more.file_scan.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.LLDocument
import com.enoch02.database.model.existsAsFile
import com.enoch02.more.R
import com.enoch02.more.file_scan.APP_PREFS_KEY
import com.enoch02.more.file_scan.DOCUMENT_COUNT_KEY
import com.enoch02.more.file_scan.PROGRESS_CHANNEL_ID
import com.enoch02.more.file_scan.PROGRESS_NOTIFICATION_ID
import com.enoch02.more.file_scan.util.createProgressNotificationChannel
import com.enoch02.more.file_scan.util.listDocsInDirectory
import com.enoch02.more.file_scan.util.makeStatusNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FileScanWorker"

@HiltWorker
class FileScanWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val documentDao: DocumentDao
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            createProgressNotificationChannel(applicationContext)
            createFileScanningNotification(applicationContext)

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            val db = documentDao.getDocumentsNonFlow()
            val persistedUriPermissions = applicationContext.contentResolver.persistedUriPermissions
            val uris = persistedUriPermissions.map { it.uri }
            val dir = uris.map {
                listDocsInDirectory(
                    context = applicationContext,
                    directoryUri = it,
                    scanned = db.map { document -> document.contentUri }
                )
            }.flatten()

            addDocsToDb(dir)
            cleanUp()
            notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
            Result.success()
        }
    }

    private suspend fun addDocsToDb(dir: List<LLDocument>) {
        documentDao.insertDocuments(dir)
    }

    //TODO: remove unused covers here
    private suspend fun cleanUp() {

    }
}

private fun createFileScanningNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
        .setContentTitle("Loading Documents")
        .setContentText("Please wait...")
        .setSmallIcon(R.drawable.ic_android_black_24dp) //TODO
        .setProgress(0, 0, true)
        .setOngoing(true)

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        //TODO: redirect to settings page
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }

    NotificationManagerCompat.from(context).notify(PROGRESS_NOTIFICATION_ID, builder.build())
}