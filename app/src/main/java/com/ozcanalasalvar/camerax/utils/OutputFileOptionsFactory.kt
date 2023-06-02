package com.ozcanalasalvar.camerax.utils

import android.app.Activity
import android.content.ContentValues
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.video.MediaStoreOutputOptions
import com.ozcanalasalvar.camerax.R

class OutputFileOptionsFactory {

    fun getPhotoOutputFileOption(activity: Activity): ImageCapture.OutputFileOptions {

        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "CameraX-capture-" + DateFormatter.getCurrentDate(DateFormats.backend)
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            val appName = activity.resources.getString(R.string.app_name)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")

        }

        return ImageCapture.OutputFileOptions.Builder(
            activity.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()
    }

    fun getVideoOutputFileOption(activity: Activity): MediaStoreOutputOptions {

        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "CameraX-recording-" + DateFormatter.getCurrentDate(DateFormats.backend)
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            val appName = activity.resources.getString(R.string.app_name)
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${appName}")
        }

        return MediaStoreOutputOptions.Builder(
            activity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()
    }
}