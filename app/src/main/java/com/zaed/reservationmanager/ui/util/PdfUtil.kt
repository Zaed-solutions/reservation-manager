package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.util.Xml
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
import com.tom_roush.fontbox.encoding.Encoding
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.font.PDTrueTypeFont
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.data.model.Customer
import org.apache.poi.ss.formula.functions.NumericFunction.COS
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object PdfUtil {
    fun List<Customer>.exportCustomersToPdf(context: Context, headers: List<String>, title: String): File? {
        try {
            val fileName = "Customers_${Date()}.pdf"
            PDFBoxResourceLoader.init(context)
            val document = PDDocument()

            // Create a new page
            val page = PDPage()
            document.addPage(page)
            val contentStream = PDPageContentStream(document, page)

            val arabicFontFile = context.assets.open("Amiri-Regular.ttf")
            val arabicFont = PDTrueTypeFont.loadTTF(document, arabicFontFile)

            // Add title
            contentStream.beginText()
            contentStream.setFont(arabicFont, 18f)
            contentStream.newLineAtOffset(200f, 750f)
            contentStream.showText("PDF Title: Customers Data")
            contentStream.endText()

            val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
            val stream = FileOutputStream(File(context.cacheDir, "temp_image.png"))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val file = File(context.cacheDir, "temp_image.png")
            val image = PDImageXObject.createFromFile(file.absolutePath, document)

            // Position and draw image
            contentStream.drawImage(image, 200f, 650f, 100f, 100f)

            // Add table headers and content
            val tableData = listOf(
                listOf("Name", "Nationality", "Phone", "Email"),
                listOf("أحمد", "مصر", "123456789", "ahmed@example.com"),
                listOf("Mohamed", "UAE", "987654321", "mohamed@example.com")
            )

            // Table positioning
            val startX = 50f
            var startY = 600f
            val rowHeight = 20f
            val cellMargin = 5f
            val tableWidth = 500f
            val numberOfColumns = 4
            val columnWidth = tableWidth / numberOfColumns

            // Draw table headers
            contentStream.setFont(arabicFont, 12f)
            for (i in tableData[0].indices) {
                contentStream.beginText()
                contentStream.newLineAtOffset(startX + (i * columnWidth) + cellMargin, startY)
                contentStream.showText(tableData[0][i])
                contentStream.endText()
            }

            // Draw table rows
            contentStream.setFont(arabicFont, 12f)
            startY -= rowHeight
            for (row in tableData.drop(1)) {
                for (i in row.indices) {
                    contentStream.beginText()
                    contentStream.newLineAtOffset(startX + (i * columnWidth) + cellMargin, startY)
                    contentStream.showText(row[i])
                    contentStream.endText()
                }
                startY -= rowHeight
            }

            contentStream.close()

            // Save PDF to device
            val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            document.save(pdfFile)
            document.close()
            return pdfFile
//            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
//            val pdfWriter = PdfWriter(file.path)
//            val pdfDocument = PdfDocument(pdfWriter)
//            val document = Document(pdfDocument)
//
//            // Convert drawable to bitmap
//            val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_foreground)
//            val stream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//            val imageData = ImageDataFactory.create(stream.toByteArray())
//            val logoImage = Image(imageData)
//
//            // Add logo above the title
//            logoImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
//            logoImage.setMaxHeight(125f)
//            logoImage.setMaxWidth(125f)
//            document.add(logoImage)
//
//            val titleParagraph = Paragraph(title)
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(18f)
//                .setMarginBottom(20f)
//            document.add(titleParagraph)
//
//            val table = Table(headers.size)
//            headers.forEach { table.addHeaderCell(it) }
//            this.forEach { customer ->
//                table.addCell(customer.name)
//                table.addCell(customer.nationality)
//                table.addCell(customer.residenceCountry)
//                table.addCell(customer.phoneNumber)
//                table.addCell(customer.email)
//            }
//            document.add(table)
//            document.close()
//            return file
        } catch (e: Exception) {
            Log.e("PdfUtil", "convertCsvToPdf: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}