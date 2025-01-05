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
import com.zaed.reservationmanager.data.model.CompanyType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    val phoneNumber1 = getCellStringValue(row.getCell(3))
                    val phoneNumber2 = getCellStringValue(row.getCell(4))
                    val email = getCellStringValue(row.getCell(5))

                    Log.d(
                        "ImportUtil",
                        "importCustomersFromExcel: $name, $nationality, $residenceCountry, $phoneNumber1, $phoneNumber2, $email"
                    )

                    // Create a Customer object
                    val customer = Customer(
                        name = name,
                        nationality = nationality,
                        residenceCountry = residenceCountry,
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
                row.createCell(3).setCellValue(customer.phoneNumber1)
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

    fun generatePaginatedArabicPdfReportForAllArrivals(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير جميع الوصول",
        title : String = "تقرير",
    ): File? {
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
        // Define page configuration
        val pageWidth = 842 //842 A4 width in points
        val pageHeight = 595 //595 A4 height in points
        val cellHeight = 20f
        val fontSize = 12f
        val titleFontSize = 16f

        val paint = Paint()
        paint.textSize = fontSize
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        val headerPaint = Paint()
        headerPaint.textSize = fontSize
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        headerPaint.textAlign = Paint.Align.CENTER

        val footerPaint = Paint()
        footerPaint.textSize = fontSize
        footerPaint.textAlign = Paint.Align.CENTER
        footerPaint.color = android.graphics.Color.DKGRAY

        val titlePaint = Paint()
        titlePaint.textSize = titleFontSize
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true

        val companyNamePaint = Paint()
        companyNamePaint.textSize = fontSize
        companyNamePaint.textAlign = Paint.Align.LEFT
        companyNamePaint.isFakeBoldText = true

        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1f

        val blackBackground = Paint()
        blackBackground.style = Paint.Style.FILL
        blackBackground.color = android.graphics.Color.LTGRAY

        // Define column positions and widths
        val columnWidths = listOf(
            30f,  // #
            100f,  // اسم الضيف
            90f,  // اسم الشركة
            90f,  // التاريخ
            50f,  // الوقت
            60f,  // نوع الحركة
            50f,  // نوع السيارة
            90f,  // بداية الرحلة
            90f,  // نهاية الرحلة
            50f,  // السعر
            50f,  // التحصيل
            50f   //الرصيد
        )
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
        var totalPrice = 0.0
        var totalCollected = 0.0
        var totalBalance = 0.0

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
                    reservation.clientName,
                    reservation.tourismCompany,
                    reservation.date.formatEpochSecondsToDateNumbers(),
                    (reservation.time + reservation.date).formatEpochSecondsToTime(),
                    reservation.type,
                    reservation.car,
                    reservation.startLocation,
                    reservation.endLocation,
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

                // Update totals for the last three columns
                totalPrice += reservation.tourismRidePrice
                totalCollected += collectedPrice
                totalBalance += (reservation.tourismRidePrice - collectedPrice)

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد التسجيلات :${reservations.size}",
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
                            canvas.drawRect(
                                172f,
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
                                172f,
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

    fun generatePaginatedArabicPdfReportForCompanyArrivals(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير الوصول للشركات",
        title : String = "تقرير حجوزات الشركة",
        companyType: CompanyType = CompanyType.TRAVEL
    ): File? {
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
        // Define page configuration
        val pageWidth = 842 //842 A4 width in points
        val pageHeight = 595 //595 A4 height in points
        val cellHeight = 20f
        val fontSize = 12f
        val titleFontSize = 16f

        val paint = Paint()
        paint.textSize = fontSize
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        val headerPaint = Paint()
        headerPaint.textSize = fontSize
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        headerPaint.textAlign = Paint.Align.CENTER

        val footerPaint = Paint()
        footerPaint.textSize = fontSize
        footerPaint.textAlign = Paint.Align.CENTER
        footerPaint.color = android.graphics.Color.DKGRAY

        val titlePaint = Paint()
        titlePaint.textSize = titleFontSize
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true

        val companyNamePaint = Paint()
        companyNamePaint.textSize = fontSize
        companyNamePaint.textAlign = Paint.Align.LEFT
        companyNamePaint.isFakeBoldText = true

        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1f

        val blackBackground = Paint()
        blackBackground.style = Paint.Style.FILL
        blackBackground.color = android.graphics.Color.LTGRAY

        // Define column positions and widths
        val columnWidths = listOf(
            30f,  // #
            100f,  // اسم الضيف
            90f,  // التاريخ
            50f,  // الوقت
            60f,  // نوع الحركة
            50f,  // نوع السيارة
            90f,  // بداية الرحلة
            90f,  // نهاية الرحلة
            50f,  // السعر
            50f,  // التحصيل
        )
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
        var totalPrice = 0.0
        var totalCollected = 0.0

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
                    if(reservation.travelCollectedAmount>0) reservation.travelCollectedAmount.toString() else reservation.tourismCollectedAmount.toString(), // التحصيل
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
                totalPrice += reservation.tourismRidePrice
                totalCollected += if(reservation.travelCollectedAmount>0) reservation.travelCollectedAmount else reservation.tourismCollectedAmount

                currentY += cellHeight
            }

            // Draw Summary Row (Total)
            if (currentIndex >= reservations.size || currentY + cellHeight > pageHeight) {
                val summaryRowData = listOf(
                    "",
                    " عدد التسجيلات :${reservations.size}",
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
                            canvas.drawRect(
                                172f,
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
                                172f,
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

    fun generatePaginatedArabicPdfReportForCompanyAccount(
        context: Context,
        reservations: List<Reservation>,
        fileName: String = "تقرير حساب الشركة",
        title : String = "تقرير",
        companyType: CompanyType = CompanyType.TRAVEL
    ): File? {
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
            "التحصيل",
            "الرصيد"
        )
        // Define page configuration
        val pageWidth = 842 //842 A4 width in points
        val pageHeight = 595 //595 A4 height in points
        val cellHeight = 20f
        val fontSize = 12f
        val titleFontSize = 16f

        val paint = Paint()
        paint.textSize = fontSize
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        val headerPaint = Paint()
        headerPaint.textSize = fontSize
        headerPaint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        headerPaint.textAlign = Paint.Align.CENTER

        val footerPaint = Paint()
        footerPaint.textSize = fontSize
        footerPaint.textAlign = Paint.Align.CENTER
        footerPaint.color = android.graphics.Color.DKGRAY

        val titlePaint = Paint()
        titlePaint.textSize = titleFontSize
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true

        val companyNamePaint = Paint()
        companyNamePaint.textSize = fontSize
        companyNamePaint.textAlign = Paint.Align.LEFT
        companyNamePaint.isFakeBoldText = true

        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1f
        borderPaint.color = android.graphics.Color.BLACK

        val blackBackground = Paint()
        blackBackground.style = Paint.Style.FILL
        blackBackground.color = android.graphics.Color.LTGRAY

        // Define column positions and widths
        val columnWidths = listOf(
            30f,  // #
            100f,  // اسم الضيف
            90f,  // التاريخ
            50f,  // الوقت
            60f,  // نوع الحركة
            50f,  // نوع السيارة
            50f,  // السعر
            50f,  // التحصيل
            50f   //الرصيد
        )
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
        when(companyType){
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
                    if(companyType==CompanyType.TRAVEL) reservation.travelRidePrice.toString() else reservation.tourismRidePrice.toString(), // السعر
                    if(companyType==CompanyType.TRAVEL) reservation.travelCollectedAmount.toString() else reservation.tourismCollectedAmount.toString(), // التحصيل
                    if(companyType==CompanyType.TRAVEL) (reservation.travelRidePrice - reservation.travelCollectedAmount).toString() else (reservation.tourismRidePrice - reservation.tourismCollectedAmount).toString()
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
                    " عدد التسجيلات :${reservations.size}",
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
                            canvas.drawRect(
                                172f,
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
                                172f,
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

                row.createCell(columnIndex++)
                    .setCellValue(reservation.date.formatEpochSecondsToDateTime())
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