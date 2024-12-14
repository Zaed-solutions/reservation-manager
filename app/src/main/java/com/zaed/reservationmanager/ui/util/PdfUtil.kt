package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import java.io.File
import java.util.Date

object PdfUtil {
    fun List<Customer>.exportCustomersToPdf(context: Context, headers: List<String>, title: String): File? {
        try {
            val fileName = "Customers_${Date()}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            val pdfWriter = PdfWriter(file.path)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Convert drawable to bitmap
            val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageData = ImageDataFactory.create(stream.toByteArray())
            val logoImage = Image(imageData)

            // Add logo above the title
            logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
            logoImage.setMaxHeight(125f)
            logoImage.setMaxWidth(125f)
            document.add(logoImage)

            val titleParagraph = Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18f)
                .setMarginBottom(20f)
            document.add(titleParagraph)

            val table = Table(headers.size)
            headers.forEach { table.addHeaderCell(it) }
            this.forEach { customer ->
                table.addCell(customer.name)
                table.addCell(customer.nationality)
                table.addCell(customer.residenceCountry)
                table.addCell(customer.phoneNumber)
                table.addCell(customer.email)
            }
            document.add(table)
            document.close()
            return file
        } catch (e: Exception) {
            Log.e("PdfUtil", "convertCsvToPdf: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}