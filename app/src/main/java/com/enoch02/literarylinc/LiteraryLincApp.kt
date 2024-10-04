package com.enoch02.literarylinc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.work.Configuration
import com.enoch02.more.WorkerFactory

@HiltAndroidApp
class LiteraryLincApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: WorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    /*override fun onCreate() {
        super.onCreate()
        WorkManager.initialize(this, workManagerConfiguration)
    }*/
}
