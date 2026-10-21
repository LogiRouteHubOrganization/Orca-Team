package data.datasource.remote.supabase

import data.datasource.remote.RemoteRouteDataSource
import data.remote.circuitbreaker.CircuitBreaker
import data.remote.dto.response.RouteDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import data.remote.ratelimit.RateLimiter
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
    private val appDispatchers: AppDispatchers,
    private val rateLimiter: RateLimiter
) : RemoteRouteDataSource {

    private val tableName = "routes"

    override suspend fun getAllRoutes(): List<RouteDto> = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "GetAllRoutes") {
                rateLimiter.acquire()
                safeApiCall { client.from(tableName).select().decodeList<RouteDto>() }
            }
            result.getOrThrow()
        }
    }

    override suspend fun getRouteById(id: String): RouteDto? = withContext(appDispatchers.io) {
        circuitBreaker.execute {
            val result = retryTemplate.execute(operationName = "GetRouteById") {
                rateLimiter.acquire()
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
                rateLimiter.acquire()
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
                rateLimiter.acquire()
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
                    rateLimiter.acquire()
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
