package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.zaed.reservationmanager.R
import java.io.File

object FileUtil {
    fun openFile(context: Context, file: File, type: String, onFailure: () -> Unit) {
        try {
            val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                setDataAndType(fileUri, type)
                flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (openFileIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(openFileIntent)
            } else {
                onFailure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun shareFile(context: Context, file: File, type: String, onFailure: () -> Unit) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                setType(type)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (shareIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(shareIntent)
            } else {
                onFailure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}