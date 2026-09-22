package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.PackageRemoteDataSource
import data.remote.dto.response.PackageDto
import data.remote.dto.request.PackageCreateRequestDto
import data.remote.dto.request.PackageUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.retryWithBackoff
import data.remote.retry.shouldRetry
import io.github.jan.supabase.postgrest.from

class PackageRemoteDataSourceImpl : PackageRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "packages"

    override suspend fun getAllPackages(): List<PackageDto> {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName).select()
                    .decodeList<PackageDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun getPackageById(id: String): PackageDto? {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .select {
                        filter {
                            eq("id", id)
                        }
                    }.decodeSingleOrNull<PackageDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun createPackage(
        packageRequest: PackageCreateRequestDto
    ): PackageDto {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .insert(packageRequest) {
                        select()
                    }
                    .decodeSingle<PackageDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun updatePackage(
        id: String,
        packageRequest: PackageUpdateRequestDto
    ): PackageDto {
        val result = retryWithBackoff(
            shouldRetry = { shouldRetry(it) }
        ) {
            safeApiCall {
                client.from(tableName)
                    .update(packageRequest) {
                        filter {
                            eq("id", id)
                        }
                        select()
                    }
                    .decodeSingle<PackageDto>()
            }
        }
        return result.getOrThrow()
    }

    override suspend fun deletePackage(id: String) {
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
