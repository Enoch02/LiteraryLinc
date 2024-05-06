package com.enoch02.database.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

object Base64Functions {
    fun encode(imagePath: String): Result<String> {
        return try {
            val imageFile = File(imagePath)
            val image = BitmapFactory.decodeStream(imageFile.inputStream(), null, null)
            val byteArrayOutputStream = ByteArrayOutputStream()

            image?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            Result.success(Base64.encodeToString(imageBytes, Base64.DEFAULT))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun decode(imageString: String): Result<Bitmap?> {
        return try {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)

            Result.success(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}