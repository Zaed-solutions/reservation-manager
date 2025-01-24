package com.zaed.reservationmanager.ui.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.opencsv.CSVReader
import com.zaed.reservationmanager.data.model.CompanyHistory
import com.zaed.reservationmanager.data.model.CompanyType
import com.zaed.reservationmanager.data.model.Customer
import com.zaed.reservationmanager.data.model.Reservation
import com.zaed.reservationmanager.ui.home.component.ReportLanguage
import com.zaed.reservationmanager.ui.util.InputValidator.validate
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.blackBackground
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.borderPaint
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.cellHeight
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.charWidth
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.footerPaint
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.headerPaint
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.minWidth
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.pageHeight
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.pageWidth
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.paint
import com.zaed.reservationmanager.ui.util.SheetUtil.PdfConfig.titlePaint
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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
                        name = name.trim(),
                        nationality = nationality.trim(),
                        residenceCountry = residenceCountry.trim(),
                        phoneNumber1 = phoneNumber.trim(),
                        email = email.trim()
                    )
                    customers.add(customer)
                }
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
                val customers = mutableMapOf<String, Customer>()

                // Start reading rows, skipping the header row (index 0)
                for (rowIndex in 1..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIndex) ?: continue

                    val name = getCellStringValue(row.getCell(0))
                    val nationality = getCellStringValue(row.getCell(1))
                    val residenceCountry = getCellStringValue(row.getCell(2))
                    val city = getCellStringValue(row.getCell(3))
                    val phoneNumber1 = getCellStringValue(row.getCell(4))
                    val phoneNumber2 = getCellStringValue(row.getCell(5))
                    val email = getCellStringValue(row.getCell(6))

                    Log.d(
                        "ImportUtil",
                        "importCustomersFromExcel: $name, $nationality, $residenceCountry, $phoneNumber1, $phoneNumber2, $email"
                    )

                    // Create a Customer object
                    val customer = Customer(
                        name = name,
                        nationality = nationality,
                        residenceCountry = residenceCountry,
                        city = city,
                        phoneNumber1 = phoneNumber1,
                        phoneNumber2 = phoneNumber2,
                        email = email
                    )
                    if (customer.validate()) {
                        customers[customer.phoneNumber1] = customer
                    } else {
                        Log.d("ImportUtil", "importCustomersFromExcel:Invalid Customer")
                    }
                }
                workbook.close()
                onImportCompleted(customers.values.toList())
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

    // Extension functions
    private fun String.truncate(maxLength: Int = 15): String =
        if (length > maxLength) substring(0, maxLength) + "..." else this


    fun List<Customer>.exportCustomersToExcel(
        context: Context,
        headers: List<String> = listOf(
            "Name",
            "Nationality",
            "Residence Country",
            "City",
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
                row.createCell(3).setCellValue(customer.city)
                row.createCell(4).setCellValue(customer.phoneNumber1)
                row.createCell(5).setCellValue(customer.phoneNumber2)
                row.createCell(6).setCellValue(customer.email)
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

    object PdfConfig {

        val minWidth = 30f
        val charWidth = 5.5f
        val pageWidth: Int = 842
        val pageHeight: Int = 595
        val cellHeight: Float = 20f
        val fontSize: Float = 10f
        val titleFontSize: Float = 16f

        val paint = Paint().apply {
            textSize = fontSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            textSize = fontSize
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val footerPaint = Paint().apply {
            textSize = fontSize
            textAlign = Paint.Align.CENTER
            color = android.graphics.Color.DKGRAY
        }
        val titlePaint = Paint().apply {
            textSize = titleFontSize
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        val companyNamePaint = Paint().apply {
            textSize = fontSize
            textAlign = Paint.Align.LEFT
            isFakeBoldText = true
        }

        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val blackBackground = Paint().apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.LTGRAY
        }
    }

    fun generatePaginatedArabicPdfReportForAllArrivals(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير جميع الوصول",
        title: String = "تقرير",
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",
            "اسم الضيف",
            "اسم الشركة",
            "التاريخ",
            "الوقت",
            "الحركة",
            "السيارة",
            "بداية الرحلة",
            "نهاية الرحلة",
            "السعر",
            "التحصيل",
            "الرصيد"
        )


        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString().truncate() },
            reservations.map { it.clientName.truncate() },
            reservations.map { it.tourismCompany.truncate() },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { (it.time + it.date).formatEpochSecondsToTime() },
            reservations.map { it.type.truncate() },
            reservations.map { it.car.truncate() },
            reservations.map { it.startLocation.truncate() },
            reservations.map { it.endLocation.truncate() },
            reservations.map { it.tourismRidePrice.toString() },
            reservations.map {
                if (it.travelCollectedAmount == 0) it.tourismCollectedAmount.toString()
                    .truncate() else it.travelCollectedAmount.toString()
            },
            reservations.map { (it.tourismRidePrice - if (it.travelCollectedAmount == 0) it.tourismCollectedAmount else it.travelCollectedAmount).toString() }
        )
        val maxLengths = headers.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }
        Log.d("SheetUtil", "generatePaginatedArabicPdfReportForAllArrivals: $maxLengths")

        // Convert character lengths to column widths

        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }

        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        // Track the totals for the last three columns
        var totalPrice = reservations.sumOf { it.tourismRidePrice }
        var totalCollected =
            reservations.sumOf { if (it.travelCollectedAmount == 0) it.tourismCollectedAmount else it.travelCollectedAmount }
        var totalBalance = totalPrice - totalCollected

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]
                val collectedPrice =
                    if (reservation.travelCollectedAmount == 0) reservation.tourismCollectedAmount else reservation.travelCollectedAmount
                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName.truncate(),
                    reservation.tourismCompany.truncate(),
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type.truncate(),
                    reservation.car.truncate(),
                    reservation.startLocation.truncate(),
                    reservation.endLocation.truncate(),
                    reservation.tourismRidePrice.toString(), // السعر
                    collectedPrice.toString(), // التحصيل
                    (reservation.tourismRidePrice - collectedPrice).toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد السجلات :${reservations.size}",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    totalPrice.toString(),
                    totalCollected.toString(),
                    totalBalance.toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 1) {
                            val space = columnWidths.subList(1, 9).sum()
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (space / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex in 2..8) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun generatePaginatedArabicPdfReportForCompanyOpenAccount(
        context: Context,
        fileName: String = "تقرير الرصيد المفتوح",
        title: String = "تقرير",
        companyType: CompanyType,
        history: List<CompanyHistory>
    ): File {
        Log.d("SheetUtil", "generatePaginatedArabicPdfReportForCompanyOpenAccount: $companyType")
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",
            "اسم الشركة",
            "عدد المشاوير",
            "قيمة المشاوير",
            "مجموع التحصيل",
            "المدفوعات",
            "الرصيد"
        )

        // Define column positions and widths
        val rides =
            history.map { it.reservations.sumOf { if (companyType == CompanyType.TOURISM) it.tourismRidePrice else it.travelRidePrice } }
        val collected =
            history.map { it.reservations.sumOf { if (companyType == CompanyType.TOURISM) it.tourismCollectedAmount else it.travelCollectedAmount } }
        val payments = history.map { it.payments.sumOf { it.amount }.toInt() }
        val balance = rides.zip(collected)
            .zip(payments) { (ride, collected), payment -> (ride - collected - payment) }

        val columnData = listOf(
            (1..history.size).map { it.toString() },
            history.map { it.company.name },
            history.map { it.reservations.size.toString() },
            rides.map { it.toString() },
            collected.map { it.toString() },
            payments.map { it.toString() },
            balance.map { it.toString() }

        )
        val maxLengths = headers.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }

        // Convert character lengths to column widths

        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }

        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0
        var totalNumberOfRides = 0
        var totalRidePrice = 0
        var totalCollected = 0
        var totalPayments = 0
        var totalBalance = 0


        while (currentIndex < history.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= history.size) break

                val currentHistory = history[currentIndex]
                val ridePrice =
                    currentHistory.reservations.sumOf { if (companyType == CompanyType.TOURISM) it.tourismRidePrice else it.travelRidePrice }
                val collectedPrice =
                    currentHistory.reservations.sumOf { if (companyType == CompanyType.TOURISM) it.tourismCollectedAmount else it.travelCollectedAmount }
                val payments = currentHistory.payments.sumOf { it.amount }.toInt()
                val balance = ridePrice - collectedPrice - payments
                totalNumberOfRides += currentHistory.reservations.size
                totalRidePrice += ridePrice
                totalCollected += collectedPrice
                totalPayments += payments
                totalBalance += balance

                val rowData = listOf(
                    currentIndex.plus(1).toString(),
                    currentHistory.company.name,//
                    currentHistory.reservations.size.toString(),
                    ridePrice.toString(),
                    collectedPrice.toString(),
                    payments.toString(),
                    balance.toString()

                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= history.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    " عدد السجلات :${history.size}",
                    "",
                    totalNumberOfRides.toString(),
                    totalRidePrice.toString(),
                    totalCollected.toString(),
                    totalPayments.toString(),
                    totalBalance.toString(),
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            val spaceWidth = columnWidths[0] + columnWidths[1]
                            canvas.drawRect(
                                startX - spaceWidth,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (spaceWidth / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - spaceWidth,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex == 1) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun generatePaginatedArabicPdfReportForProfits(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير الارباح",
        title: String = "تقرير",
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",
            "اسم الضيف",
            "التاريخ",
            "الوقت",
            "الحركة",
            "السيارة",
            "السعر",
            "الربح"
        )

        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString() },
            reservations.map { it.clientName },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { (it.time + it.date).formatEpochSecondsToTime() },
            reservations.map { it.type },
            reservations.map { it.car },
            reservations.map { it.tourismRidePrice.toString() },
            reservations.map { (it.tourismRidePrice - it.travelRidePrice).toString() },
        )
        val maxLengths = headers.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }

        // Convert character lengths to column widths

        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }

        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        // Track the totals for the last three columns
        val totalProfit = reservations.sumOf { it.tourismRidePrice - it.travelRidePrice }

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]
                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName,
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type,
                    reservation.car,
                    reservation.tourismRidePrice.toString(), // السعر
                    (reservation.tourismRidePrice - reservation.travelRidePrice).toString()//profit
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",//index
                    " عدد السجلات :${reservations.size}", //records number
                    "",//records number
                    "",//records number
                    "اجمالى الربح : ${NumberFormat.getCurrencyInstance().apply { maximumFractionDigits =0 }.format(totalProfit)}",
                    "",//total profit
                    "",//total profit
                    "",//total profit
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }
                        if (columnIndex == 1) {
                            canvas.drawRect(
                                startX - columnWidths[1] - columnWidths[2] - columnWidths[3],
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - columnWidths[1] - columnWidths[2] - columnWidths[3],
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex == 4) {
                            val leftSpace =
                                columnWidths[4] + columnWidths[5] + columnWidths[6] + columnWidths[7]
                            canvas.drawRect(
                                startX - leftSpace,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (leftSpace / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - leftSpace,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            return@forEachIndexed
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun generatePaginatedArabicPdfReportForCompanyArrivals(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير الوصول للشركة",
        title: String = "تقرير حجوزات الشركة",
        companyType: CompanyType = CompanyType.TRAVEL
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",
            "اسم الضيف",
            "التاريخ",
            "الوقت",
            "الحركة",
            "السيارة",
            "بداية الرحلة",
            "نهاية الرحلة",
            "السعر",
            "التحصيل",
        )

        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString() },
            reservations.map { it.clientName },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { (it.time + it.date).formatEpochSecondsToTime() },
            reservations.map { it.type },
            reservations.map { it.car },
            reservations.map { it.startLocation },
            reservations.map { it.endLocation },
            reservations.map { if (companyType ==CompanyType.TRAVEL) it.travelRidePrice.toString() else it.tourismRidePrice.toString()  },
            reservations.map { if (companyType ==CompanyType.TRAVEL) it.travelCollectedAmount.toString() else it.tourismCollectedAmount.toString() },
        )
        val maxLengths = headers.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }

        // Convert character lengths to column widths
        val minWidth = 30f
        val charWidth = 7f
        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }

        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        // Track the totals for the last three columns
        var totalPrice = reservations.sumOf { it.tourismRidePrice }
        var totalCollected =
            reservations.sumOf { if (companyType==CompanyType.TRAVEL) it.travelCollectedAmount else it.tourismCollectedAmount }

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]

                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName,
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type,
                    reservation.car,
                    reservation.startLocation,
                    reservation.endLocation,
                    reservation.tourismRidePrice.toString(), // السعر
                    if (reservation.travelCollectedAmount > 0) reservation.travelCollectedAmount.toString() else reservation.tourismCollectedAmount.toString(), // التحصيل
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد السجلات :${reservations.size}",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    totalPrice.toString(),
                    totalCollected.toString(),
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 1) {
                            val space = columnWidths.subList(1, 8).sum()
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (space / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex in 2..7) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    private fun List<String>.calculateMaxLengths(columnData: List<List<String>>): List<Int> {
        return this.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }
    }

    fun generatePaginatedArabicPdfReportForCompanyAccountCompact(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير حساب شركة",
        title: String = "تقرير",
        language: ReportLanguage = ReportLanguage.Arabic,
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        var headers: List<String> = listOf(
            "#",
            "اسم الضيف",
            "إسم الموظف",
            "تاريخ أول حركة",
            "عدد الحركات",
            "السعر",
            "المدفوع",
            "الرصيد"
        )
        if(language == ReportLanguage.English){
            headers=listOf(
                "#",
                "Guest Name",
                "Employee",
                "First Movement",
                "Nu. Of Mov.",
                "Price",
                "Paid",
                "Balance"
            )
        }

        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString() },
            reservations.map { it.clientName },
            reservations.map { it.tourismEmployee },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { it.numberOfRides.toString() },
            reservations.map { it.tourismRidePrice.toString() },
            reservations.map { it.tourismCollectedAmount.toString() },
            reservations.map { (it.tourismRidePrice - it.tourismCollectedAmount).toString() }
        )
        val maxLengths = headers.calculateMaxLengths(columnData)
        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        val totalPrice = reservations.sumOf { it.tourismRidePrice }
        val totalCollected = reservations.sumOf { it.tourismCollectedAmount }
        val totalBalance = totalPrice - totalCollected

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]

                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName,
                    reservation.tourismEmployee,
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    reservation.numberOfRides.toString(),
                    reservation.tourismRidePrice.toString(), // السعر
                    reservation.tourismCollectedAmount.toString(), // التحصيل
                    (reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                // Update totals for the last three columns


                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    if(language == ReportLanguage.Arabic)" عدد السجلات :${reservations.size}" else "Record Count :${reservations.size}",
                    "",
                    "",
                    reservations.sumOf { it.numberOfRides }.toString(),
                    totalPrice.toString(),
                    totalCollected.toString(),
                    totalBalance.toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        val space = columnWidths.subList(1, 4).sum()
                        if (columnIndex == 1) {
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (space / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex in 2..3) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun generatePaginatedArabicPdfReportForCompanyAccountDetailed(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير حساب شركة",
        title: String = "تقرير",
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",//0
            "اسم الضيف",//1
            "إسم الموظف",//2
            "التاريخ",//3
            "الوقت",//4
            "الحركة",//5
            "السيارة",//6
            "من",//7
            "إلى",//8
            "السعر",//9
            "المدفوع ",//10
            "الرصيد"//11
        )

        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString() },
            reservations.map { it.clientName.truncate() },
            reservations.map { it.tourismEmployee.truncate() },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { (it.time + it.date).formatEpochSecondsToTime() },
            reservations.map { it.type },
            reservations.map { it.car },
            reservations.map { it.startLocation.truncate() },
            reservations.map { it.endLocation.truncate() },
            reservations.map { it.tourismRidePrice.toString() },
            reservations.map { it.tourismCollectedAmount.toString() },
            reservations.map { (it.tourismRidePrice - it.tourismCollectedAmount).toString() }
        )
        val maxLengths = headers.calculateMaxLengths(columnData)

        // Convert character lengths to column widths
        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }


        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        val totalPrice = reservations.sumOf { it.tourismRidePrice }
        val totalCollected = reservations.sumOf { it.tourismCollectedAmount }
        val totalBalance = totalPrice - totalCollected

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]

                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName.truncate(),
                    reservation.tourismEmployee.truncate(),
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type,
                    reservation.car,
                    reservation.startLocation.truncate(),
                    reservation.endLocation.truncate(),
                    reservation.tourismRidePrice.toString(), // السعر
                    reservation.tourismCollectedAmount.toString(), // التحصيل
                    (reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                // Update totals for the last three columns


                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد السجلات :${reservations.size}",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    totalPrice.toString(),
                    totalCollected.toString(),
                    totalBalance.toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        val space = columnWidths.subList(1, 9).sum()
                        if (columnIndex == 1) {
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (space / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex in 2..8) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun generatePaginatedArabicPdfReportForCompanyAccount(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير حساب شركة",
        title: String = "تقرير",
        companyType: CompanyType = CompanyType.TRAVEL
    ): File {
        // Initialize the PDF document
        val pdfDocument = PdfDocument()
        val headers: List<String> = listOf(
            "#",
            "اسم الضيف",
            "التاريخ",
            "الوقت",
            "الحركة",
            "السيارة",
            "السعر",
            "المدفوعات",
            "الرصيد"
        )

        // Define column positions and widths
        val columnData = listOf(
            reservations.map { it.reservationNumber.toString() },
            reservations.map { it.clientName },
            reservations.map { it.date.formatEpochSecondsToDateNumbers() },
            reservations.map { (it.time + it.date).formatEpochSecondsToTime() },
            reservations.map { it.type },
            reservations.map { it.car },
            reservations.map { if (companyType == CompanyType.TRAVEL) it.travelRidePrice.toString() else it.tourismRidePrice.toString() },
            reservations.map { if (companyType == CompanyType.TRAVEL) it.travelCollectedAmount.toString() else it.tourismCollectedAmount.toString() },
            reservations.map { if (companyType == CompanyType.TRAVEL) (it.travelRidePrice - it.travelCollectedAmount).toString() else (it.tourismRidePrice - it.tourismCollectedAmount).toString() }
        )
        val maxLengths = headers.mapIndexed { index, header ->
            maxOf(
                header.length,
                columnData[index].maxOfOrNull { it.length } ?: 0
            )
        }

        // Convert character lengths to column widths

        val columnWidths = maxLengths.map { max(minWidth, it * charWidth) }


        // Calculate the total width of the columns
        val totalColumnsWidth = columnWidths.sum()

        // Calculate the start position for the columns to center them on the page
        val startXOffset = pageWidth - ((pageWidth - totalColumnsWidth) / 2)

        // Create column start positions relative to the startXOffset
        val columnStartPositions =
            columnWidths.runningFold(startXOffset) { acc, width -> acc - width }

        val maxRowsPerPage = ((pageHeight - 140) / cellHeight).toInt()

        var currentPage = 1
        var currentIndex = 0

        // Track the totals for the last three columns
        var totalPrice = 0
        var totalCollected = 0
        var totalBalance = 0
        when (companyType) {
            CompanyType.TOURISM -> {
                totalPrice = reservations.sumOf { it.tourismRidePrice }
                totalCollected = reservations.sumOf { it.tourismCollectedAmount }
                totalBalance = totalPrice - totalCollected
            }

            CompanyType.TRAVEL -> {
                totalPrice = reservations.sumOf { it.travelRidePrice }
                totalCollected = reservations.sumOf { it.travelCollectedAmount }
                totalBalance = totalPrice - totalCollected
            }
        }

        while (currentIndex < reservations.size) {
            // Start a new page
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Draw Title
            canvas.drawText(
                title,
                (pageWidth / 2).toFloat(),
                50f,
                titlePaint
            )

            // Draw Header Row
            var currentY = 70f
            columnStartPositions.zip(columnWidths).forEachIndexed { index, (startX, width) ->
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    blackBackground
                )
                canvas.drawText(
                    headers[index],
                    startX - (width / 2),
                    currentY + (cellHeight / 2) + 4,
                    headerPaint
                )
                canvas.drawRect(
                    startX - width,
                    currentY,
                    startX,
                    currentY + cellHeight,
                    borderPaint
                )
            }
            currentY += cellHeight

            // Draw Data Rows
            for (i in 0 until maxRowsPerPage) {
                if (currentIndex >= reservations.size) break

                val reservation = reservations[currentIndex]

                val rowData = listOf(
                    reservation.reservationNumber.toString(),
                    reservation.clientName,
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type,
                    reservation.car,
                    if (companyType == CompanyType.TRAVEL) reservation.travelRidePrice.toString() else reservation.tourismRidePrice.toString(), // السعر
                    if (companyType == CompanyType.TRAVEL) reservation.travelCollectedAmount.toString() else reservation.tourismCollectedAmount.toString(), // التحصيل
                    if (companyType == CompanyType.TRAVEL) (reservation.travelRidePrice - reservation.travelCollectedAmount).toString() else (reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        if (columnIndex == 0) {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else {
                            canvas.drawText(
                                rowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                paint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
                currentIndex++

                // Update totals for the last three columns


                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد السجلات :${reservations.size}",
                    "",
                    "",
                    "",
                    "",
                    totalPrice.toString(),
                    totalCollected.toString(),
                    totalBalance.toString()
                )
                columnStartPositions.zip(columnWidths)
                    .forEachIndexed { columnIndex, (startX, width) ->
                        val space = columnWidths.subList(1, 6).sum()
                        if (columnIndex == 1) {
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (space / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - space,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        } else if (columnIndex in 2..5) {
                            return@forEachIndexed
                        } else {
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                blackBackground
                            )
                            canvas.drawText(
                                summaryRowData[columnIndex],
                                startX - (width / 2),
                                currentY + (cellHeight / 2) + 4,
                                headerPaint
                            )
                            canvas.drawRect(
                                startX - width,
                                currentY,
                                startX,
                                currentY + cellHeight,
                                borderPaint
                            )
                        }

                    }
            }

            val footer = " صفحة $currentPage"
            canvas.drawText(footer, (pageWidth / 2).toFloat(), pageHeight - 25f, footerPaint)


            // Finish the current page
            pdfDocument.finishPage(page)
            currentPage++
        }

        // Save the PDF
        val filePath = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "$fileName  ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.pdf"
        )
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()

        // Notify user
        println("PDF report generated successfully at: $filePath")
        return filePath // Return the generated file
    }

    fun List<Reservation>.exportReservationsToExcel(
        context: Context,
        headers: List<String>,
        isAllRides: Boolean = false,
        isTourismCompany: Boolean = false,
        isTravelCompany: Boolean = false
    ): File? {
        try {
            // Create a new workbook and sheet
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Reservations")

            // Create a header row
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }
            this.forEachIndexed { rowIndex, reservation ->
                val row = sheet.createRow(rowIndex + 1)
                var columnIndex = 0
                //date
                row.createCell(columnIndex++)
                    .setCellValue(reservation.date.formatEpochSecondsToDate())
                //time
                row.createCell(columnIndex++)
                    .setCellValue(reservation.time.formatEpochSecondsToTime())
                row.createCell(columnIndex++).setCellValue(reservation.type)
                row.createCell(columnIndex++).setCellValue(reservation.car)
                row.createCell(columnIndex++).setCellValue(reservation.clientName)
                row.createCell(columnIndex++).setCellValue(reservation.tourismCompany)
                row.createCell(columnIndex++).setCellValue(reservation.tourismRidePrice.toString())
                row.createCell(columnIndex++)
                    .setCellValue(reservation.tourismCollectedAmount.toString())
                row.createCell(columnIndex++)
                    .setCellValue((reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString())
                row.createCell(columnIndex++).setCellValue(reservation.travelCompany)
                row.createCell(columnIndex++).setCellValue(reservation.travelRidePrice.toString())
                row.createCell(columnIndex++)
                    .setCellValue(reservation.travelCollectedAmount.toString())
                row.createCell(columnIndex++)
                    .setCellValue((reservation.travelRidePrice - reservation.travelCollectedAmount).toString())
            }

            // Create a file in the external storage directory
            val fileName = "Reservations_${System.currentTimeMillis()}.xlsx"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            // Write the workbook to the file
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }

            workbook.close()
            return file // Return the generated file
        } catch (e: Exception) {
            Log.e("SheetUtil", "exportReservationsToExcel: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

}