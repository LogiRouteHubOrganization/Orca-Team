package parser

import model.Route
import java.io.File

fun parseRoute(filePath: String): List<Route> {

    val routes = mutableListOf<Route>()

    for (line in File(filePath).readLines()) {

        if (line.startsWith("routeId") || line.isBlank()) {
            continue
        }

        val data = line.split(",")

        routes.add(
            Route(
                data[0],
                data[1],
                data[2],
                data[3],
                data[4]
            )
        )
    }

    return routes
}