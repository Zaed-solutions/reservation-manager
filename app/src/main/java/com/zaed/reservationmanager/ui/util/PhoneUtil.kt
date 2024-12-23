package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object PhoneUtil {
    fun sendWhatsappMessage(
        context: Context,
        phoneNumber: String,
        message: String,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit
    ) {
        val uri = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}")

        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setPackage("com.whatsapp")
        }
        val whatsappBusinessIntent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
            setPackage("com.whatsapp.w4b")
        }

        val resolveInfos = listOf(whatsappIntent, whatsappBusinessIntent).mapNotNull {
            if (it.resolveActivity(context.packageManager) != null) it else null
        }

        if (resolveInfos.isNotEmpty()) {
            val chooserIntent = Intent.createChooser(resolveInfos[0], "Choose WhatsApp version")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, resolveInfos.subList(1, resolveInfos.size).toTypedArray())
            context.startActivity(chooserIntent)
            onSuccess()
        } else {
            onFailure()
        }
    }
}