package com.enoch02.resources

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ResourcesModule {
    @Provides
    fun providesBitmapManager() = BitmapManager.getInstance()
}
