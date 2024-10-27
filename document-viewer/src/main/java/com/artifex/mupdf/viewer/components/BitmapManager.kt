package com.artifex.mupdf.viewer.components

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

class BitmapManager private constructor() {
    private val trackedBitmaps = mutableSetOf<WeakReference<Bitmap>>()
    private val bitmapCache: LruCache<String, Bitmap>

    init {
        // Get max available VM memory
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        Log.d("BitmapManager", "Max Memory: ${maxMemory / 1024} MB")
        // Use 1/8th of the available memory for this cache
        val cacheSize = maxMemory / 8

        bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // Size in kilobytes
                return bitmap.allocationByteCount / 1024
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }

    private fun trackBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            trackedBitmaps.add(WeakReference(it))
        }
    }

    fun cacheBitmap(key: String, bitmap: Bitmap) {
        bitmapCache.put(key, bitmap)
        trackBitmap(bitmap)
    }

    fun getCachedBitmap(key: String): Bitmap? = bitmapCache.get(key)

    suspend fun releaseAllBitmaps() = withContext(Dispatchers.IO) {
        // Clear LRU cache
        bitmapCache.evictAll()

        // Recycle tracked bitmaps
        trackedBitmaps.forEach { bitmapRef ->
            bitmapRef.get()?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
        }
        trackedBitmaps.clear()
    }

    fun releaseBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: BitmapManager? = null

        fun getInstance(): BitmapManager =
            instance ?: synchronized(this) {
                instance ?: BitmapManager().also { instance = it }
            }
    }
}

// Extension function for convenient cleanup in Activities/Fragments
suspend fun BitmapManager.cleanup() {
    releaseAllBitmaps()
}