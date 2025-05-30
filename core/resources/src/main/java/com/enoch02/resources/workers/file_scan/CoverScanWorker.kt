package com.enoch02.resources.workers.file_scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.existsAsFile
import com.enoch02.resources.R
import com.enoch02.resources.createProgressNotificationChannel
import com.enoch02.resources.sendFinalProgressNotification
import com.enoch02.resources.workers.COVER_SCAN_NOTIFICATION_ID
import com.enoch02.resources.workers.PROGRESS_CHANNEL_ID
import com.enoch02.resources.workers.file_scan.util.generateThumbnail
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "CoverScanWorker"

@HiltWorker
class CoverScanWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val bookCoverRepository: BookCoverRepository,
    private val documentDao: DocumentDao
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        createProgressNotificationChannel(applicationContext)

        val coversSnapshot = bookCoverRepository.getCoverFolderSnapshot()
        val documents = documentDao.getDocumentsNonFlow()
        val totalDocsCount = documents.size

        documents.forEachIndexed { index, document ->
            if (!coversSnapshot.containsKey(document.cover)) {
                val bitmap = document.contentUri?.generateThumbnail(applicationContext)

                if (bitmap != null) {
                    bookCoverRepository.saveCoverFromBitmap(
                        bitmap = bitmap,
                        name = document.id
                    )
                        .onSuccess { name ->
                            documentDao.updateDocument(document.copy(cover = name))
                            bitmap.recycle()

                            Log.d(TAG, "getCovers: cover created for ${document.name}")
                        }
                } else {
                    Log.d(TAG, "getCovers: cover not created for ${document.name}")
                }

                val progress = (((index + 1).toDouble() / totalDocsCount.toDouble()) * 100).toInt()

                createFileScanningNotification(
                    context = applicationContext,
                    progress = progress
                )
            } else {
                Log.d(TAG, "getNewCovers: ${document.name} has a cover")
            }
        }

        val missingFiles = documents
            .filterNot { document -> document.existsAsFile(applicationContext) }


        if (missingFiles.isNotEmpty()) {
            bookCoverRepository.cleanUp(missingFiles.map { it.id })
            // remove db entries for missing files
            missingFiles.forEach { document ->
                documentDao.deleteDocument(document.contentUri.toString())
            }
        } else {
            Log.i(TAG, "doWork: No unused cover to cleanup")
        }
        sendFinalProgressNotification(applicationContext, COVER_SCAN_NOTIFICATION_ID)

        return Result.success()
    }
}

private fun createFileScanningNotification(context: Context, progress: Int) {
    val builder = NotificationCompat.Builder(context, PROGRESS_CHANNEL_ID)
        .setContentTitle("Loading Covers")
        .setContentText("Progress: $progress%")
        .setSmallIcon(R.drawable.app_icon_svg)
        .setProgress(
            100,
            progress,
            false
        ) // Maximum progress is 100, and we set the current progress
        .setOngoing(true) // Make the notification ongoing so it can't be swiped away

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    NotificationManagerCompat.from(context).notify(COVER_SCAN_NOTIFICATION_ID, builder.build())
}