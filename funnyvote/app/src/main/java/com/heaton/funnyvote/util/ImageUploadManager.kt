package com.heaton.funnyvote.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageUploadManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun compressAndUploadImage(
        context: Context,
        uri: Uri,
        storagePath: String,
        maxDimension: Int = 800,
        quality: Int = 75
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("無法開啟圖片檔案：$uri")

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                throw IllegalArgumentException("無法解析圖片格式")
            }

            // 1. 等比例縮放
            val width = originalBitmap.width
            val height = originalBitmap.height
            val bitmapToCompress = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetWidth: Int
                val targetHeight: Int
                if (ratio > 1f) {
                    targetWidth = maxDimension
                    targetHeight = (maxDimension / ratio).toInt()
                } else {
                    targetHeight = maxDimension
                    targetWidth = (maxDimension * ratio).toInt()
                }
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            } else {
                originalBitmap
            }

            // 2. 本地 JPEG 壓縮
            val outputStream = ByteArrayOutputStream()
            bitmapToCompress.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            outputStream.close()

            // 3. 上傳至 Firebase Storage 並附加 30 天公開快取標頭 (防止流量超額)
            val storageRef = storage.reference.child(storagePath)
            val metadata = storageMetadata {
                contentType = "image/jpeg"
                cacheControl = "public, max-age=2592000"
            }

            storageRef.putBytes(bytes, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            downloadUrl
        }
    }
}
