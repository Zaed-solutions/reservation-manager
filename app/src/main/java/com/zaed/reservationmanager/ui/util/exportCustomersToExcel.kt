package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.os.Environment
import com.zaed.reservationmanager.data.model.Customer
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Date

fun exportCustomersToExcel(context: Context, customers: List<Customer>): File? {
    try {
        // Create a new workbook and sheet
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Customers")

        // Create a header row
        val headerRow = sheet.createRow(0)
        val headers = listOf("ID", "Name", "Nationality", "Residence Country", "Phone Number", "Email", "Created At")
        for ((index, header) in headers.withIndex()) {
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
        }

        // Populate rows with customer data
        for ((rowIndex, customer) in customers.withIndex()) {
            val row: Row = sheet.createRow(rowIndex + 1)
            row.createCell(0).setCellValue(customer.id)
            row.createCell(1).setCellValue(customer.name)
            row.createCell(2).setCellValue(customer.nationality)
            row.createCell(3).setCellValue(customer.residenceCountry)
            row.createCell(4).setCellValue(customer.phoneNumber)
            row.createCell(5).setCellValue(customer.email)
            row.createCell(6).setCellValue(customer.createdAtEpochSeconds.formatEpochSecondsToDate()) // Convert Date to String
        }

        // Create a file in the external storage directory
        val fileName = "Customers_${System.currentTimeMillis()}.xlsx"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        // Write the workbook to the file
        FileOutputStream(file).use { outputStream ->
            workbook.write(outputStream)
        }

        workbook.close()
        return file // Return the generated file
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null // Return null if an error occurred
}
