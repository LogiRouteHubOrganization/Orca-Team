package data.remote.datasource

import data.remote.SupabaseClientProvider
import data.remote.dto.RouteDto
import data.remote.dto.request.RouteRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class RouteRemoteDataSourceImpl(
    private val tableName: String = "routes"
) : RouteRemoteDataSource {

    private val client = SupabaseClientProvider.client

    override suspend fun getAllRoutes(): List<RouteDto> =
        safeApiCall {
            client
                .from(tableName)
                .select()
                .decodeList<RouteDto>()
        }

    override suspend fun getRouteById(id: String): RouteDto? =
        safeApiCall {
            client
                .from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<RouteDto>()
        }

    override suspend fun createRoute(
        routeRequest: RouteRequestDto
    ): RouteDto =
        safeApiCall {
            client
                .from(tableName)
                .insert(routeRequest) {
                    select()
                }
                .decodeSingle<RouteDto>()
        }

    override suspend fun updateRoute(
        id: String,
        routeRequest: RouteRequestDto
    ): RouteDto =
        safeApiCall {
            client
                .from(tableName)
                .update(routeRequest) {
                    filter {
                        eq("id", id)
                    }
                    select()
                }
                .decodeSingle<RouteDto>()
        }

    override suspend fun deleteRoute(id: String) {
        safeApiCall {
            client
                .from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
        }
    }
}