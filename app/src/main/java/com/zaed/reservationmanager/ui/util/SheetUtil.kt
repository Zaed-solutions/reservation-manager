package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Ride
import io.github.voytech.tabulate.api.builder.dsl.header
import io.github.voytech.tabulate.model.attributes.column.columnWidth
import io.github.voytech.tabulate.template.tabulate
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
        return null // Return null if an error occurred
    }

    fun List<Ride>.exportCsv(headers: Array<String>): String {
        val fileName = "rides_${Date()}.csv"
        val totalSelling = this.sumOf { it.sellingPrice }
        val totalCollected = this.sumOf { it.collectedPrice }
        this.tabulate(fileName) {
            name = "Rides List"
            attributes {
                columnWidth { auto = true }
            }
            columns {
                column("dateCol")
                column("timeCol")
                column("typeCol")
                column("carCol")
                column("customerNameCol")
                column("sellingPriceCol")
                column("collectedPriceCol")
                column("balanceCol")
            }
            rows {
                header(*headers)
                this@exportCsv.forEach { ride ->
                    newRow {
                        cell("date") { value = ride.date.formatEpochSecondsToDate() }
                        cell("time") { value = ride.date.formatEpochSecondsToDate() }
                        cell("type") { value = ride.type }
                        cell("car") { value = ride.car }
                        cell("customerName") { value = "Placeholder Customer Name" }
                        cell("sellingPrice") { value = ride.sellingPrice }
                        cell("collectedPrice") { value = ride.collectedPrice }
                        cell("balance") { value = ride.sellingPrice - ride.collectedPrice }
                    }
                }

                newRow {
                    cell("Total") { value = "Total Selling: " }
                    repeat(headers.size - 4) { cell("placeholder$it") { value = "" } }
                    cell("totalSellingPrice") { value = totalSelling }
                    cell("totalCollectedPrice") { value = totalCollected }
                    cell("totalBalance") { value = totalSelling - totalCollected }
                }
            }
        }
        return fileName
    }
}