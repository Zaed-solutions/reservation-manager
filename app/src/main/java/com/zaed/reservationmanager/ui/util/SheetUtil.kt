package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Ride
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Date

object SheetUtil {
    fun List<Customer>.exportCustomersToExcel(
        context: Context,
        headers: List<String> = listOf(
            "Name",
            "Nationality",
            "Residence Country",
            "Phone Number",
            "Email"
        )
    ): File? {
        try {
            // Create a new workbook and sheet
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Customers")

            // Create a header row
            val headerRow = sheet.createRow(0)
            for ((index, header) in headers.withIndex()) {
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // Populate rows with customer data
            for ((rowIndex, customer) in this.withIndex()) {
                val row: Row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(customer.name)
                row.createCell(1).setCellValue(customer.nationality)
                row.createCell(2).setCellValue(customer.residenceCountry)
                row.createCell(3).setCellValue(customer.phoneNumber)
                row.createCell(4).setCellValue(customer.email)
            }

            // Create a file in the external storage directory
            val fileName = "Customers_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // Write the workbook to the file
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            workbook.close()
            Log.d("SheetUtil", "exportCustomersToExcel: $fileName, ${file.path}")
            return file // Return the generated file
        } catch (e: Exception) {
            Log.e("SheetUtil", "exportCustomersToExcel: ${e.message}")
            e.printStackTrace()
        }
        return null
    }

    fun List<Ride>.exportRidesAsCsv(
        context: Context,
        headers: List<String>,
        isAllRides: Boolean = false,
        isTourismCompany: Boolean = false,
        isTravelCompany: Boolean = false
    ): File? {
        try {
            val fileName = "Rides_${Date()}.csv"
            val totalSelling = this.sumOf { it.sellingPrice }
            val totalBuying = this.sumOf { it.buyingPrice }
            val totalCollected = this.sumOf { it.collectedPrice }
            val totalBalance = this.sumOf {
                when {
                    isAllRides -> it.sellingPrice - it.buyingPrice
                    isTourismCompany -> it.sellingPrice - it.collectedPrice
                    isTravelCompany -> it.buyingPrice - it.collectedPrice
                    else -> 0.0
                }
            }
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Rides")

            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            this.forEachIndexed { index, ride ->
                val row: Row = sheet.createRow(index + 1)
                var columnIndex = 0
                row.createCell(columnIndex++).setCellValue(ride.date.formatEpochSecondsToDateTime())
                row.createCell(columnIndex++).setCellValue(ride.type)
                row.createCell(columnIndex++).setCellValue(ride.car)
                row.createCell(columnIndex++).setCellValue(ride.clientName)
                if (!isTravelCompany) {
                    row.createCell(columnIndex++).setCellValue(ride.sellingPrice)
                }
                if (!isTourismCompany) {
                    row.createCell(columnIndex++).setCellValue(ride.buyingPrice)
                }
                row.createCell(columnIndex++).setCellValue(ride.collectedPrice)
                row.createCell(columnIndex).setCellValue(
                    when {
                        isAllRides -> ride.sellingPrice - ride.buyingPrice
                        isTourismCompany -> ride.sellingPrice - ride.collectedPrice
                        isTravelCompany -> ride.buyingPrice - ride.collectedPrice
                        else -> 0.0
                    }
                )
            }
            val row = sheet.createRow(this.size + 1)
            var columnIndex = 0
            row.createCell(columnIndex++).setCellValue("Total")
            repeat(3) {
                row.createCell(columnIndex++).setCellValue("")
            }
            if (!isTravelCompany) {
                row.createCell(columnIndex++).setCellValue(totalSelling)
            }
            if (!isTourismCompany) {
                row.createCell(columnIndex++).setCellValue(totalBuying)
            }
            row.createCell(columnIndex++).setCellValue(totalCollected)
            row.createCell(columnIndex).setCellValue(totalBalance)
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            return file
        } catch (e: Exception) {
            Log.e("SheetUtil", "exportRidesAsCsv: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}