package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.RouteRemoteDataSource
import data.remote.dto.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class RouteRemoteDataSourceImpl : RouteRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "routes"

    override suspend fun getAllRoutes(): List<RouteDto> {
        return safeApiCall {
            client.from(tableName)
                .select()
                .decodeList<RouteDto>()
        }
    }

    override suspend fun getRouteById(id: String): RouteDto? {
        return safeApiCall {
            client.from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<RouteDto>()
        }
    }

    override suspend fun createRoute(
        routeRequest: RouteCreateRequestDto
    ): RouteDto {
        return safeApiCall {
            client.from(tableName)
                .insert(routeRequest) {
                    select()
                }
                .decodeSingle<RouteDto>()
        }
    }

    override suspend fun updateRoute(
        id: String,
        routeRequest: RouteUpdateRequestDto
    ): RouteDto {
        return safeApiCall {
            client.from(tableName)
                .update(routeRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<RouteDto>()
        }
    }

    override suspend fun deleteRoute(id: String) {
        safeApiCall {
            client.from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
        }
    }
}
