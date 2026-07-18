package parser

import java.io.File
import model.Warehouse

object WarehouseParser {

    //Parses the warehouses CSV file, handles edge cases, and returns a clean list of Warehouse models.

    fun parseWarehouses(filePath: String): List<Warehouse> {
        val warehouses = mutableListOf<Warehouse>()
        val file = File(filePath)

        //  Ensure the file exists before processing to prevent application crashes
        if (!file.exists()) {
            println("Error: File not found at $filePath")
            return warehouses
        }

        val lines = file.readLines()

        //  Drop the first header line and ignore completely blank or whitespace-only lines
        val dataLines = lines.drop(1).filter { it.isNotBlank() }

        for ((index, line) in dataLines.withIndex()) {

            // Edge Case 1: Split the row by commas and trim leading/trailing whitespaces from each token
            val tokens = line.split(",").map { it.trim() }

            // Edge Case 2: Validate column count and critical data presence (Must have at least 3 columns)

            if (tokens.size < 3 || tokens[0].isEmpty() || tokens[1].isEmpty()) {
                println("Diagnostic Warning [Line ${index + 2}]: Column count mismatch or missing critical data. Skipping row: \"$line\"")
                continue
            }

            val id = tokens[0]
            val name = tokens[1]

            // Edge Case 4: Standardize priority/string values case-insensitively

            val regionalZone = if (tokens[2].isEmpty()) "UNKNOWN" else tokens[2].uppercase()

            // Add the successfully validated and cleaned record to the final list
            warehouses.add(Warehouse(id, name, regionalZone))
        }

        return warehouses
    }

}