package repository.csv

import dataHolder.RouteRaw
import parser.RouteParser
import repository.RouteRepository
import java.io.File

class CsvRouteRepository(
    private val filePath: String
) : RouteRepository {

    override fun getAll(): List<RouteRaw> {

        if (!File(filePath).exists()) {
            println("Diagnostic Warning: File $filePath does not exist.")
            return emptyList()
        }


        return RouteParser(filePath)
    }
}