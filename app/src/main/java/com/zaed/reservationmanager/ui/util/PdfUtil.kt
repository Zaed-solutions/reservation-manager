package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Table
import java.io.File

object PdfUtil {
    fun convertCsvToPdf(context: Context, csvFileName: String, pdfFileName: String) {
        try{
            val csvLines = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), csvFileName).readLines()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
            val pdfWriter = PdfWriter(file.path)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            val header = csvLines.first().split(",")
            val table = Table(header.size)
            header.forEach { table.addHeaderCell(it) }
            csvLines.drop(1).forEach { line ->
                line.split(",").forEach { cell ->
                    table.addCell(cell)
                }
            }
            document.add(table)
            document.close()
        } catch(e: Exception){
            Log.e("PdfUtil", "convertCsvToPdf: ${e.message}")
            e.printStackTrace()
        }
    }
}