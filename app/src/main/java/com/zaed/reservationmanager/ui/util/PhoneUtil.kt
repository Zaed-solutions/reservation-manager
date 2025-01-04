package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast

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

    fun saveToContacts(
        context: Context,
        name: String,
        phoneNumber1: String,
        phoneNumber2: String,
        email: String,
        company: String=""
    ) {
        // Create an intent to insert a new contact
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name) // Add the name
            putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber1) // Add the primary phone number

            // Add the second phone number if provided
            if (phoneNumber2.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, phoneNumber2)
            }
            if(email.isNotBlank()){
                putExtra(ContactsContract.Intents.Insert.EMAIL, email)
            }
            if(company.isNotBlank()){
                putExtra(ContactsContract.Intents.Insert.COMPANY, company)
            }

        }

        // Check if there's an app to handle this intent
        if (intent.resolveActivity(context.packageManager) != null) {
            // Launch the contact editor
            context.startActivity(intent)
        } else {
            // Notify the user if no app is available
            Toast.makeText(context, "No app available to save contacts", Toast.LENGTH_SHORT).show()
        }
    }


}