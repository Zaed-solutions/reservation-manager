package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.zaed.reservationmanager.data.model.Customer
import java.io.File
import java.util.Date

fun exportCustomersToPdf(context: Context, customers: List<Customer>): File? {
    try {
        // إنشاء ملف PDF
        val fileName = "Customers_${System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        // إعداد الوثيقة
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        pdfDocument.defaultPageSize = PageSize.A4
        val document = Document(pdfDocument)
        document.setMargins(0f, 10f, 0f, 10f)

        // عنوان الصفحة
        val title = Paragraph("Customer List")
            .setBold()
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(title)

        // إعداد الجدول
        val columnWidths = floatArrayOf(0.5f, 2f, 1.5f, 2f, 2f, 2.5f, 1.5f)
        val table = Table(columnWidths)
        table.setWidth(UnitValue.createPercentValue(100f)) // عرض الجدول 100% من الصفحة
        table.setHorizontalAlignment(HorizontalAlignment.CENTER)
        // رؤوس الجدول
        val headers = listOf("No", "Name", "Nationality", "Residence Country", "Phone", "Email", "Created At")
        for (header in headers) {
            val cell = Cell().add(Paragraph(header).setBold().setTextAlignment(TextAlignment.CENTER))
            cell.setBackgroundColor(DeviceRgb(63, 81, 181))
            cell.setFontColor(DeviceRgb(255, 255, 255))
            table.addCell(cell)
        }

        // إضافة البيانات
        var counter = 1
        for (customer in customers) {
            table.addCell(Cell().add(Paragraph(counter.toString())))
            table.addCell(Cell().add(Paragraph(customer.name)))
            table.addCell(Cell().add(Paragraph(customer.nationality)))
            table.addCell(Cell().add(Paragraph(customer.residenceCountry)))
            table.addCell(Cell().add(Paragraph(customer.phoneNumber)))
            table.addCell(
                Cell().add(
                    Paragraph(customer.email)
                        .setFontColor(DeviceRgb(0, 102, 204))
                        .setUnderline()
                )
            )
            table.addCell(Cell().add(Paragraph(customer.createdAt.toString())))
            counter++
        }

        // إضافة الجدول إلى الوثيقة
        document.add(table)

        // إغلاق الوثيقة
        document.close()

        return file
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}
