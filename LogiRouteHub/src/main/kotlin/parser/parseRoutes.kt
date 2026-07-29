package parser
import java.io.File
import dataHolder.RoutesRaw


private const val HEADER_ROW_COUNT = 1
private const val EXPECTED_COLUMN_COUNT = 5
private const val ORIGIN_COLUMN_INDEX = 0
private const val DESTINATION_COLUMN_INDEX = 1
private const val ROUTE_NAME_COLUMN_INDEX = 2
private const val DISTANCE_COLUMN_INDEX = 3
private const val DELAY_COLUMN_INDEX = 4
private const val CSV_SEPARATOR = ","


    fun parseRoutes(filePath: String): List<RoutesRaw> {
        val routes = mutableListOf<RoutesRaw>()
        val lines = File(filePath).readLines()
        for ((rowNumber, line) in lines.withIndex()) {
            if (skipLine(rowNumber, line)) continue
            val values = getValues(line)
            if (!checkColumns(values, rowNumber)) continue
            if (!checkEmpty(values, rowNumber)) continue
            if (!checkNumbers(values, rowNumber)) continue
            routes.add(makeRoute(values))
        }
        return routes
    }

    private fun skipLine(row: Int, line: String): Boolean {
        return row < HEADER_ROW_COUNT || line.isBlank()
    }

    private fun getValues(line: String): List<String> {
        return line.split(CSV_SEPARATOR ).map { it.trim() }
    }

    private fun checkColumns(values: List<String>, row: Int): Boolean {
        if (values.size != EXPECTED_COLUMN_COUNT) {
            println("Warning: Row ${row + 1} has wrong number of columns")
            return false
        }
        return true
    }

    private fun checkEmpty(values: List<String>, row: Int): Boolean {
        if (values.any { it.isEmpty() }) {
            println("Warning: Row ${row + 1} contains empty value")
            return false
        }
        return true
    }

    private fun checkNumbers(values: List<String>, row: Int): Boolean {
        if (values[DISTANCE_COLUMN_INDEX].toDoubleOrNull() == null) {
            println("Warning: Row ${row + 1} has invalid distance")
            return false
        }

        if (values[DELAY_COLUMN_INDEX].toIntOrNull() == null) {
            println("Warning: Row ${row + 1} has invalid delay")
            return false
        }

        return true
    }

    private fun makeRoute(values: List<String>): RoutesRaw {
        return RoutesRaw(
            values[ORIGIN_COLUMN_INDEX],
            values[DESTINATION_COLUMN_INDEX],
            values[ROUTE_NAME_COLUMN_INDEX],
            values[DISTANCE_COLUMN_INDEX],
            values[DELAY_COLUMN_INDEX]
        )
    }

