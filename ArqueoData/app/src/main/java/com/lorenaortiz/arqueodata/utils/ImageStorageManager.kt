package com.lorenaortiz.arqueodata.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageStorageManager(private val context: Context) {

    fun saveImage(uri: Uri, prefix: String): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val imagesDir = getImagesDirectory()
            val imageFile = File(imagesDir, fileName)
            
            FileOutputStream(imageFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            
            return imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun getImageFile(path: String): File? {
        return try {
            File(path)
        } catch (e: Exception) {
            null
        }
    }

    private fun getImagesDirectory(): File {
        val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ArqueoData")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return imagesDir
    }

    fun deleteImage(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.delete()
        } catch (e: Exception) {
            false
        }
    }
} 