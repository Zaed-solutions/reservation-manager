package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.util.InputValidator.validate
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Date

object SheetUtil {

    fun importCustomersFromCSV(
        context: Context,
        fileUri: Uri,
        onImportCompleted: (List<Customer>) -> Unit
    ) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(fileUri)

            inputStream?.use { stream ->
                val reader = CSVReader(InputStreamReader(stream))
                val customers = mutableListOf<Customer>()

                // Skip header row
                val rows = reader.readAll().drop(1)

                // Read each row
                for (row in rows) {
                    if (row.size < 5) continue // Ensure row has all required columns

                    val name = row[0]
                    val nationality = row[1]
                    val residenceCountry = row[2]
                    val phoneNumber = row[3]
                    val email = row[4]

                    // Create a Customer object
                    val customer = Customer(
                        name = name,
                        nationality = nationality,
                        residenceCountry = residenceCountry,
                        phoneNumber = phoneNumber,
                        email = email
                    )
                    customers.add(customer)
                }

                // Return the imported customers through the callback
                onImportCompleted(customers)
            } ?: Log.e("ImportUtil", "Failed to open input stream")
        } catch (e: Exception) {
            Log.e("ImportUtil", "importCustomersFromCSV: ${e.message}")
            e.printStackTrace()
        }
    }
    fun importCustomersFromExcel(
        context: Context,
        fileUri: Uri,
        onImportCompleted: (List<Customer>) -> Unit
    ) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(fileUri)

            inputStream?.use { stream ->
                val workbook = XSSFWorkbook(stream)
                val sheet = workbook.getSheetAt(0) // Assuming data is in the first sheet
                val customers = mutableListOf<Customer>()

                // Start reading rows, skipping the header row (index 0)
                for (rowIndex in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue

                    val name = getCellStringValue(row.getCell(0))
                    val nationality = getCellStringValue(row.getCell(1))
                    val residenceCountry = getCellStringValue(row.getCell(2))
                    val phoneNumber = getCellStringValue(row.getCell(3))
                    val email = getCellStringValue(row.getCell(4))

                    Log.d("ImportUtil", "importCustomersFromExcel: $name, $nationality, $residenceCountry, $phoneNumber, $email")

                    // Create a Customer object
                    val customer = Customer(
                        name = name,
                        nationality = nationality,
                        residenceCountry = residenceCountry,
                        phoneNumber = phoneNumber,
                        email = email
                    )
                    if(customer.validate()){
                        customers.add(customer)
                    }else {
                        Log.d("ImportUtil", "importCustomersFromExcel:Invalid Customer")
                    }
                }

                workbook.close()

                // Return the imported customers through the callback
                onImportCompleted(customers)
            } ?: Log.e("ImportUtil", "Failed to open input stream")
        } catch (e: Exception) {
            Log.e("ImportUtil", "importCustomersFromExcel: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Helper function to get string value from a cell, regardless of its type.
     */
    private fun getCellStringValue(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString() // Format date if necessary
                } else {
                    cell.numericCellValue.toLong().toString() // Convert number to string
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cellFormula // You may need to evaluate the formula
            else -> "" // Handle BLANK or NULL cells
        }
    }
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

    fun List<Reservation>.exportReservationsAsCSV(
        context: Context,
        headers: List<String>,
        isAllRides: Boolean = false,
        isTourismCompany: Boolean = false,
        isTravelCompany: Boolean = false
    ): File? {
        try {
            val fileName = "Reservations_${Date()}.csv"
            val totalSelling = this.sumOf { it.sellingPrice }
            val totalBuying = this.sumOf { it.buyingPrice }
            val totalCollected = this.sumOf { it.collectedAmount }
            val totalBalance = this.sumOf {
                when {
                    isAllRides -> it.sellingPrice - it.buyingPrice
                    isTourismCompany -> it.sellingPrice - it.collectedAmount
                    isTravelCompany -> it.buyingPrice - it.collectedAmount
                    else -> 0.0
                }
            }
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Reservations")

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
                row.createCell(columnIndex++).setCellValue(ride.collectedAmount)
                row.createCell(columnIndex).setCellValue(
                    when {
                        isAllRides -> ride.sellingPrice - ride.buyingPrice
                        isTourismCompany -> ride.sellingPrice - ride.collectedAmount
                        isTravelCompany -> ride.buyingPrice - ride.collectedAmount
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