
package data.datasource.remote.supabase

import data.datasource.remote.RemotePackageDataSource
import data.remote.dto.request.PackageCreateRequestDto
import data.remote.dto.request.PackageUpdateRequestDto
import data.remote.dto.response.PackageDto
import data.remote.retry.RetryableRemoteDatasource
import data.remote.utils.safeApiCall
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabasePackageDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource
) : RemotePackageDataSource {

    private val tableName = "packages"

    override suspend fun getAllPackages(): List<PackageDto> =
        withContext(Dispatchers.IO) {
            val result = retryTemplate.execute(operationName = "GetAllPackages") {
                safeApiCall { client.from(tableName).select().decodeList<PackageDto>() }
            }
            result.getOrThrow()
        }

    override suspend fun getPackageById(id: String): PackageDto? =
        withContext(Dispatchers.IO) {
            val result = retryTemplate.execute(operationName = "GetPackageById") {
                safeApiCall { client.from(tableName).select {
                            filter { eq("id", id) }
                        }.decodeSingleOrNull<PackageDto>()
                }
            }
            result.getOrThrow()
        }

    override suspend fun createPackage(
        packageRequest: PackageCreateRequestDto
    ): PackageDto = withContext(Dispatchers.IO) {
        val result = retryTemplate.execute(operationName = "CreatePackage") {
            safeApiCall {
                client.from(tableName).insert(packageRequest) { select() }.decodeSingle<PackageDto>()
            }
        }

        result.getOrThrow()
    }

    override suspend fun updatePackage(
        id: String,
        packageRequest: PackageUpdateRequestDto
    ): PackageDto = withContext(Dispatchers.IO) {
        val result = retryTemplate.execute(
            operationName = "UpdatePackage"
        ) {
            safeApiCall { client.from(tableName).update(packageRequest) {
                        filter { eq("id", id) }
                        select()
                    }.decodeSingle<PackageDto>()
            }
        }

        result.getOrThrow()
    }

    override suspend fun deletePackage(id: String) {
        withContext(Dispatchers.IO) {
            val result = retryTemplate.execute(operationName = "DeletePackage") {
                safeApiCall { client.from(tableName).delete {
                            filter { eq("id", id) }
                        }
                }
            }

            result.getOrThrow()
        }
    }
}
