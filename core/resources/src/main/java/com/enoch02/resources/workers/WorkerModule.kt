package com.enoch02.resources.workers

import android.content.Context
import androidx.work.WorkManager
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.resources.workers.backup_restore.BackupRestoreRepository
import com.enoch02.resources.workers.backup_restore.WorkManagerBackRestoreRepository
import com.enoch02.resources.workers.file_scan.DocumentScanRepository
import com.enoch02.resources.workers.file_scan.WorkManagerDocumentScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun providesDocumentScanRepository(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): DocumentScanRepository =
        WorkManagerDocumentScanRepository(context, workManager)

    @Provides
    @Singleton
    fun providesBackupRestoreRepository(workManager: WorkManager): BackupRestoreRepository =
        WorkManagerBackRestoreRepository(workManager)

    @Provides
    fun providesWorkerFactory(
        bookDao: BookDao,
        documentDao: DocumentDao,
        bookCoverRepository: BookCoverRepository,
        csvManager: CSVManager
    ) =
        WorkerFactory(bookDao, documentDao, bookCoverRepository, csvManager)
}
