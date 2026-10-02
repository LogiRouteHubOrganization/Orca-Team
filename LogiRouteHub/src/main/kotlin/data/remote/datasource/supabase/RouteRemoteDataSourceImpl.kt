package data.remote.datasource.supabase

import data.remote.datasource.RouteRemoteDataSource
import data.remote.dto.response.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class RouteRemoteDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource
) : RouteRemoteDataSource {
    private val tableName = "routes"

    override suspend fun getAllRoutes(): List<RouteDto> {
        val result = retryTemplate.execute(
            operationName = "GetAllRoutes"
        ) {
            safeApiCall {
                client.from(tableName)
                    .select()
                    .decodeList<RouteDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun getRouteById(id: String): RouteDto? {
        val result = retryTemplate.execute(
            operationName = "GetRouteById"
        ) {
            safeApiCall {
                client.from(tableName).select {
                        filter {
                            eq("id", id)
                        }
                    }
                    .decodeSingleOrNull<RouteDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun createRoute(
        routeRequest: RouteCreateRequestDto
    ): RouteDto {
        val result = retryTemplate.execute(
            operationName = "CreateRoute"
        ) {
            safeApiCall {
                client.from(tableName).insert(routeRequest) {
                        select()
                    }
                    .decodeSingle<RouteDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun updateRoute(
        id: String,
        routeRequest: RouteUpdateRequestDto
    ): RouteDto {
        val result = retryTemplate.execute(
            operationName = "UpdateRoute"
        ) {
            safeApiCall {
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

        return result.getOrThrow()
    }

    override suspend fun deleteRoute(id: String) {
        val result = retryTemplate.execute(
            operationName = "DeleteRoute"
        ) {
            safeApiCall {
                client.from(tableName).delete { filter {
                            eq("id", id)
                        }
                    }
            }
        }

        result.getOrThrow()
    }
}
