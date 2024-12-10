import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import com.zaed.reservationmanager.data.model.Customer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import android.text.TextUtils

fun generateCustomerPdf(context: Context, customers: List<Customer>) {
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 page width in points
    val pageHeight = 842 // A4 page height in points

    val paint = TextPaint().apply {
        textSize = 12f
        isAntiAlias = true
    }

    val titlePaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
    }

    val headerPaint = Paint().apply {
        textSize = 14f
        isFakeBoldText = true
        color = 0xFF6200EE.toInt()
    }

    val margin = 20f
    val columnWidths = listOf(80f, 120f, 80f, 80f, 100f, 120f) // Adjusted widths
    val columnHeaders = listOf("ID", "Name", "Nationality", "Country", "Phone", "Email")

    var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
    var canvas = page.canvas

    // Draw the title
    canvas.drawText("Customer List", margin, margin + 20f, titlePaint)

    // Draw the headers
    var yPosition = margin + 50f
    var xPosition = margin
    for (i in columnHeaders.indices) {
        canvas.drawText(columnHeaders[i], xPosition, yPosition, headerPaint)
        xPosition += columnWidths[i]
    }

    // Line under headers
    yPosition += 10f
    canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, paint)
    yPosition += 20f

    for ((index, customer) in customers.withIndex()) {
        xPosition = margin
        val rowData = listOf(
            customer.id,
            customer.name,
            customer.nationality,
            customer.residenceCountry,
            customer.phoneNumber,
            customer.email
        )

        for (i in rowData.indices) {
            val text = rowData[i]
            val columnWidth = columnWidths[i]
            val truncatedText = TextUtils.ellipsize(text, paint, columnWidth - 10, TextUtils.TruncateAt.END).toString()

            canvas.drawText(truncatedText, xPosition, yPosition, paint)
            xPosition += columnWidths[i]
        }

        yPosition += 20f

        // Check for page break
        if (yPosition + 20f > pageHeight - margin) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 2).create())
            canvas = page.canvas
            yPosition = margin + 50f // Reset for the next page
        }
    }

    pdfDocument.finishPage(page)

    // Save the PDF
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "CustomerList.pdf")
    try {
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        println("PDF saved at: ${file.absolutePath}")
    } catch (e: IOException) {
        e.printStackTrace()
        println("Error while saving the PDF: ${e.message}")
    }
}
