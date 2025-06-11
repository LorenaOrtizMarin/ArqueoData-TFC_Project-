package com.lorenaortiz.arqueodata.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidades para el manejo de archivos en la aplicación.
 * Proporcionamos funciones para crear y gestionar archivos de imagen.
 */
object FileUtils {
    /**
     * Creamos un archivo de imagen temporal con un nombre único basado en la fecha y hora.
     * @param context Contexto de la aplicación
     * @return URI del archivo de imagen creado o null si hay un error
     */
    fun createImageFile(context: Context): Uri? {
        return try {
            // Generamos un timestamp único para el nombre del archivo
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            
            // Obtenemos el directorio de imágenes de la aplicación
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                ?: throw IllegalStateException("No se pudo acceder al directorio de imágenes")
            
            // Creamos el archivo temporal
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            Uri.fromFile(imageFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 