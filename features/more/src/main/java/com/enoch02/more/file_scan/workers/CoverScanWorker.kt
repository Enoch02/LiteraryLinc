package com.enoch02.more.file_scan.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.more.file_scan.util.generateThumbnail
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
        val coversSnapshot = bookCoverRepository.getCoverFolderSnapshot()

        documentDao.getDocumentsNonFlow().forEach { document ->
            if (!coversSnapshot.containsKey(document.cover)) {
                val bitmap = document.contentUri?.let { it1 ->
                    generateThumbnail(
                        applicationContext,
                        it1
                    )
                }
                if (bitmap != null) {
                    bookCoverRepository.saveCoverFromBitmap(
                        bitmap = bitmap,
                        name = document.id
                    )
                        .onSuccess { name ->
                            documentDao.updateDocument(document.copy(cover = name))
                            Log.d(TAG, "getCovers: cover created for ${document.name}")
                        }
                } else {
                    Log.d(TAG, "getCovers: cover not created for ${document.name}")
                }
            } else {
                Log.d(TAG, "getNewCovers: ${document.name} has a cover")
            }
        }

        return Result.success()
    }
}