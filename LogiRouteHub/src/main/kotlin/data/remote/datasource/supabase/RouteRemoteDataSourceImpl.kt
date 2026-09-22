package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.RouteRemoteDataSource
import data.remote.dto.response.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.retryWithBackoff
import data.remote.retry.shouldRetry
import io.github.jan.supabase.postgrest.from

class RouteRemoteDataSourceImpl : RouteRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "routes"

    override suspend fun getAllRoutes(): List<RouteDto> {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
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
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .select {
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
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .insert(routeRequest) {
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
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
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
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .delete {
                        filter {
                            eq("id", id)
                        }
                    }
            }
        }
        result.getOrThrow()
    }
}
