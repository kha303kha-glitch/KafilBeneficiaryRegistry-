package com.kafil.beneficiaries.data.excel

import android.content.Context
import android.net.Uri
import com.kafil.beneficiaries.data.local.Beneficiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.IOException

data class ExcelImportSummary(
    val parsedRows: List<Beneficiary>,
    val skippedRows: Int
)

object ExcelHelper {

    /**
     * Configures StAX XML factories for Apache POI XSSFWorkbook on Android ART.
     */
    fun initStaxForAndroid() {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    private val arabicHeaders = listOf(
        "الرمز الفريد",
        "الاسم الكامل",
        "الرقم الوطني",
        "رقم الهاتف",
        "العنوان وموقعه",
        "اسم البرنامج",
        "قيمة التحويل",
        "تاريخ البداية",
        "تاريخ النهاية",
        "ملاحظات عامة"
    )

    private val columnWidths = listOf(
        18, 28, 18, 16, 30, 24, 15, 15, 15, 32
    )

    suspend fun exportToUri(
        context: Context,
        uri: Uri,
        beneficiaries: List<Beneficiary>
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            initStaxForAndroid()
            val workbook = XSSFWorkbook()
            workbook.use { wb ->
                val sheet = wb.createSheet("المستفيدين").apply {
                    isRightToLeft = true
                }

                val headerFont = wb.createFont().apply {
                    bold = true
                    color = IndexedColors.WHITE.index
                    fontHeightInPoints = 12
                }
                val headerStyle = wb.createCellStyle().apply {
                    fillForegroundColor = IndexedColors.DARK_TEAL.index
                    fillPattern = FillPatternType.SOLID_FOREGROUND
                    alignment = HorizontalAlignment.CENTER
                    verticalAlignment = VerticalAlignment.CENTER
                    setFont(headerFont)
                    borderBottom = BorderStyle.THIN
                    borderTop = BorderStyle.THIN
                    borderLeft = BorderStyle.THIN
                    borderRight = BorderStyle.THIN
                }

                val textStyle = wb.createCellStyle().apply {
                    alignment = HorizontalAlignment.RIGHT
                    verticalAlignment = VerticalAlignment.CENTER
                    wrapText = true
                }

                val amountStyle = wb.createCellStyle().apply {
                    alignment = HorizontalAlignment.CENTER
                    verticalAlignment = VerticalAlignment.CENTER
                    dataFormat = wb.createDataFormat().getFormat("#,##0.00")
                }

                // Header row
                val headerRow = sheet.createRow(0).apply { heightInPoints = 28f }
                arabicHeaders.forEachIndexed { colIndex, title ->
                    headerRow.createCell(colIndex).apply {
                        setCellValue(title)
                        cellStyle = headerStyle
                    }
                    // Note: avoid Sheet.autoSizeColumn() on Android as java.awt.font is absent
                    sheet.setColumnWidth(colIndex, columnWidths[colIndex] * 256)
                }

                // Data rows
                beneficiaries.forEachIndexed { index, item ->
                    val row = sheet.createRow(index + 1).apply { heightInPoints = 22f }
                    row.createTextCell(0, item.uniqueCode, textStyle)
                    row.createTextCell(1, item.fullName, textStyle)
                    row.createTextCell(2, item.nationalId, textStyle)
                    row.createTextCell(3, item.phoneNumber, textStyle)
                    row.createTextCell(4, item.addressLocation, textStyle)
                    row.createTextCell(5, item.programName, textStyle)
                    row.createCell(6).apply {
                        setCellValue(item.transferAmount)
                        cellStyle = amountStyle
                    }
                    row.createTextCell(7, item.startDate, textStyle)
                    row.createTextCell(8, item.endDate, textStyle)
                    row.createTextCell(9, item.notes, textStyle)
                }

                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("تعذر فتح ملف الوجهة عبر إطار الوصول للتخزين (SAF)")

                outputStream.use { out ->
                    wb.write(out)
                    out.flush()
                }
                beneficiaries.size
            }
        }
    }

    suspend fun importFromUri(
        context: Context,
        uri: Uri
    ): Result<ExcelImportSummary> = withContext(Dispatchers.IO) {
        runCatching {
            initStaxForAndroid()
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("تعذر قراءة ملف Excel عبر إطار الوصول للتخزين (SAF)")

            inputStream.use { input ->
                val workbook = XSSFWorkbook(input)
                workbook.use { wb ->
                    val sheet = wb.getSheetAt(0)
                    val formatter = DataFormatter()
                    val headerMap = resolveHeaderMap(sheet.getRow(0), formatter)

                    val parsed = mutableListOf<Beneficiary>()
                    var skipped = 0

                    for (rowIdx in 1..sheet.lastRowNum) {
                        val row = sheet.getRow(rowIdx) ?: continue

                        val uniqueCode = row.cellString(headerMap["uniqueCode"] ?: 0, formatter)
                        val fullName = row.cellString(headerMap["fullName"] ?: 1, formatter)
                        val nationalId = row.cellString(headerMap["nationalId"] ?: 2, formatter)
                        val phoneNumber = row.cellString(headerMap["phoneNumber"] ?: 3, formatter)
                        val addressLocation = row.cellString(headerMap["addressLocation"] ?: 4, formatter)
                        val programName = row.cellString(headerMap["programName"] ?: 5, formatter)
                        val transferAmount = row.cellDouble(headerMap["transferAmount"] ?: 6, formatter)
                        val startDate = row.cellString(headerMap["startDate"] ?: 7, formatter)
                        val endDate = row.cellString(headerMap["endDate"] ?: 8, formatter)
                        val notes = row.cellString(headerMap["notes"] ?: 9, formatter)

                        // Skip completely blank or invalid rows missing core identifiers
                        if (uniqueCode.isBlank() && fullName.isBlank() && nationalId.isBlank()) {
                            continue
                        }
                        if (uniqueCode.isBlank() || fullName.isBlank()) {
                            skipped++
                            continue
                        }

                        parsed.add(
                            Beneficiary(
                                uniqueCode = uniqueCode,
                                fullName = fullName,
                                nationalId = nationalId,
                                phoneNumber = phoneNumber,
                                addressLocation = addressLocation,
                                programName = programName,
                                transferAmount = transferAmount,
                                startDate = startDate,
                                endDate = endDate,
                                notes = notes
                            )
                        )
                    }

                    ExcelImportSummary(parsedRows = parsed, skippedRows = skipped)
                }
            }
        }
    }

    private fun resolveHeaderMap(headerRow: Row?, formatter: DataFormatter): Map<String, Int> {
        if (headerRow == null) return emptyMap()
        val map = mutableMapOf<String, Int>()
        for (cellIdx in 0 until headerRow.lastCellNum.toInt()) {
            val raw = formatter.formatCellValue(headerRow.getCell(cellIdx)).trim().lowercase()
            when {
                raw.contains("الرمز") || raw == "uniquecode" || raw == "unique_code" -> map["uniqueCode"] = cellIdx
                raw.contains("الاسم") || raw == "fullname" || raw == "full_name" -> map["fullName"] = cellIdx
                raw.contains("الوطني") || raw == "nationalid" || raw == "national_id" -> map["nationalId"] = cellIdx
                raw.contains("الهاتف") || raw.contains("الجوال") || raw == "phonenumber" || raw == "phone_number" -> map["phoneNumber"] = cellIdx
                raw.contains("العنوان") || raw.contains("موقع") || raw == "addresslocation" || raw == "address_location" -> map["addressLocation"] = cellIdx
                raw.contains("البرنامج") || raw == "programname" || raw == "program_name" -> map["programName"] = cellIdx
                raw.contains("قيمة") || raw.contains("مبلغ") || raw == "transferamount" || raw == "transfer_amount" -> map["transferAmount"] = cellIdx
                raw.contains("البداية") || raw == "startdate" || raw == "start_date" -> map["startDate"] = cellIdx
                raw.contains("النهاية") || raw == "enddate" || raw == "end_date" -> map["endDate"] = cellIdx
                raw.contains("ملاحظ") || raw == "notes" -> map["notes"] = cellIdx
            }
        }
        return map
    }

    private fun Row.createTextCell(colIndex: Int, value: String, style: org.apache.poi.ss.usermodel.CellStyle): Cell {
        return createCell(colIndex).apply {
            setCellValue(value)
            cellStyle = style
        }
    }

    private fun Row.cellString(colIndex: Int, formatter: DataFormatter): String {
        val cell = getCell(colIndex) ?: return ""
        return formatter.formatCellValue(cell).trim()
    }

    private fun Row.cellDouble(colIndex: Int, formatter: DataFormatter): Double {
        val cell = getCell(colIndex) ?: return 0.0
        return runCatching {
            when (cell.cellType) {
                org.apache.poi.ss.usermodel.CellType.NUMERIC -> cell.numericCellValue
                else -> formatter.formatCellValue(cell)
                    .replace(",", "")
                    .replace("٫", ".")
                    .trim()
                    .toDoubleOrNull() ?: 0.0
            }
        }.getOrDefault(0.0)
    }
}
