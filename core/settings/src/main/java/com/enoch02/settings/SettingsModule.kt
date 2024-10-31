package com.enoch02.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SettingsModuleModule {
    @Provides
    @Singleton
    fun providesSettingsRepository(@ApplicationContext context: Context) =
        SettingsRepository(context)
}
