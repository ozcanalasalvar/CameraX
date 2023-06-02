package com.ozcanalasalvar.camerax.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.File

object FileManager {

    fun createFile(path: String) {
        val dir = File(path)

        if (!dir.exists()) {
            dir.mkdirs()
        }
    }


    fun deleteFile(path: String) {
        val file = File(path)
        file.deleteRecursively()
    }


    fun getAbsolutePathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(contentUri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            if (cursor == null) {
                return null
            }
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: RuntimeException) {
            Log.e("VideoViewerFragment", String.format("Failed in getting absolute path for Uri %s with Exception %s", contentUri.toString(), e.toString()))
            null
        } finally {
            cursor?.close()
        }
    }


    fun getFileSizeFromUri(context: Context, contentUri: Uri): Long? {
        val cursor = context.contentResolver.query(contentUri, null, null, null, null) ?: return null

        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()

        cursor.use {
            return it.getLong(sizeIndex)
        }
    }

    fun deleteFile(context: Context, uri: Uri) {
        val contentResolver = context.contentResolver
        try {
            contentResolver.delete(uri, null, null);
        } catch (_: Exception) {

        }
    }


}