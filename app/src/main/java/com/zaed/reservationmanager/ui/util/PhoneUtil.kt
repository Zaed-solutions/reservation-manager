package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.zaed.reservationmanager.R
import kotlinx.coroutines.launch

object PhoneUtil {
    fun sendWhatsappMessage(context: Context, phoneNumber: String, message: String, onFailure: () -> Unit){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/${phoneNumber}?text=${Uri.encode(message)}")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            onFailure()
        }
    }
    fun messageNumber(context: Context, phoneNumber: String, onFailure: () -> Unit){
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/${phoneNumber}")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            onFailure
        }
    }
}