package parser

import model.Route
import java.io.File

fun parseRoute(filePath: String): List<Route> {

    val routes = mutableListOf<Route>()

    val file = File(filePath)

    for ((index, line) in file.readLines().withIndex()) {

        // Skip header and empty lines
        if (index == 0 || line.isBlank()) {
            continue
        }

        try {

            // Split data and remove spaces
            val data = line.split(",").map { it.trim() }


            // Check number of columns
            if (data.size != 5) {
                println("Warning: Row ${index + 1} has wrong number of columns")
                continue
            }


            // Check empty values
            if (data[0].isEmpty() ||
                data[1].isEmpty() ||
                data[2].isEmpty() ||
                data[3].isEmpty() ||
                data[4].isEmpty()) {

                println("Warning: Row ${index + 1} contains empty value")
                continue
            }


            // Check numeric values
            if (data[3].toDoubleOrNull() == null ||
                data[4].toIntOrNull() == null) {

                println("Warning: Row ${index + 1} has invalid number")
                continue
            }


            // Create Route object
            val route = Route(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4]
            )


            // Add route to list
            routes.add(route)


        } catch (e: Exception) {

            println("Warning: Error in row ${index + 1}")
        }
    }


    return routes
}