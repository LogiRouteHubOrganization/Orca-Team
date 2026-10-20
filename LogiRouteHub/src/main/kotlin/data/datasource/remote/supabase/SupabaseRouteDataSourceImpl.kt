package data.datasource.remote.supabase

import data.datasource.remote.RemoteRouteDataSource
import data.remote.circuitbreaker.CircuitBreaker
import data.remote.dto.response.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import dispatcher.AppDispatchers
import kotlinx.coroutines.withContext

class SupabaseRouteDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource,
    private val circuitBreaker: CircuitBreaker,
    private val appDispatchers: AppDispatchers
) : RemoteRouteDataSource {

    private val tableName = "routes"

    override suspend fun getAllRoutes(): List<RouteDto> = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "GetAllRoutes") {
                safeApiCall { client.from(tableName).select().decodeList<RouteDto>() }
            }
            result.getOrThrow()
        }
    }

    override suspend fun getRouteById(id: String): RouteDto? = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "GetRouteById") {
                safeApiCall {
                    client.from(tableName).select {
                        filter { eq("id", id) }
                    }.decodeSingleOrNull<RouteDto>()
                }
            }
            result.getOrThrow()
        }
    }

    override suspend fun createRoute(
        routeRequest: RouteCreateRequestDto
    ): RouteDto = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "CreateRoute") {
                safeApiCall {
                    client.from(tableName).insert(routeRequest) { select() }.decodeSingle<RouteDto>()
                }
            }
            result.getOrThrow()
        }
    }

    override suspend fun updateRoute(
        id: String,
        routeRequest: RouteUpdateRequestDto
    ): RouteDto = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "UpdateRoute") {
                safeApiCall {
                    client.from(tableName).update(routeRequest) {
                        filter { eq("id", id) }
                        select()
                    }.decodeSingle<RouteDto>()
                }
            }
            result.getOrThrow()
        }
    }

    override suspend fun deleteRoute(id: String) {
        withContext(appDispatchers.io) {
            circuitBreaker.execute {
                val result = retryTemplate.execute(operationName = "DeleteRoute") {
                    safeApiCall {
                        client.from(tableName).delete {
                            filter { eq("id", id) }
                        }
                    }
                }
                result.getOrThrow()
            }
        }
    }
}
