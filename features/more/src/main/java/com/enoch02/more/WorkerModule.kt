package com.enoch02.more

import android.content.Context
import androidx.work.WorkManager
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.more.backup_restore.data.BackupRestoreRepository
import com.enoch02.more.backup_restore.data.WorkManagerBackRestoreRepository
import com.enoch02.more.file_scan.data.DocumentScanRepository
import com.enoch02.more.file_scan.data.WorkManagerDocumentScanRepository
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
    fun providesDocumentScanRepository(workManager: WorkManager): DocumentScanRepository =
        WorkManagerDocumentScanRepository(workManager)

    @Provides
    @Singleton
    fun providesBackupRestoreRepository(workManager: WorkManager): BackupRestoreRepository =
        WorkManagerBackRestoreRepository(workManager)

    @Provides
    fun providesWorkerFactory(
        documentDao: DocumentDao,
        bookCoverRepository: BookCoverRepository,
        csvManager: CSVManager
    ) =
        WorkerFactory(documentDao, bookCoverRepository, csvManager)
}
