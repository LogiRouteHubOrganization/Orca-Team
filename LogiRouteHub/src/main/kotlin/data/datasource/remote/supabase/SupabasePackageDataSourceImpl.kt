package data.datasource.remote.supabase


import data.datasource.remote.RemotePackageDataSource
import data.remote.dto.response.PackageDto
import data.remote.dto.request.PackageCreateRequestDto
import data.remote.dto.request.PackageUpdateRequestDto
import data.remote.utils.safeApiCall
import data.remote.retry.RetryableRemoteDatasource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabasePackageDataSourceImpl(
    private val client: SupabaseClient,
    private val retryTemplate: RetryableRemoteDatasource
) : RemotePackageDataSource {
    private val tableName = "packages"

    override suspend fun getAllPackages(): List<PackageDto> {
        val result = retryTemplate.execute(
            operationName = "GetAllPackages"
        ) {
            safeApiCall {
                client.from(tableName)
                    .select()
                    .decodeList<PackageDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun getPackageById(id: String): PackageDto? {
        val result = retryTemplate.execute(
            operationName = "GetPackageById"
        ) {
            safeApiCall {
                client.from(tableName)
                    .select {
                        filter {
                            eq("id", id)
                        }
                    }
                    .decodeSingleOrNull<PackageDto>()
            }
        }

        return result.getOrThrow()
    }

    override suspend fun createPackage(
        packageRequest: PackageCreateRequestDto
    ): PackageDto {
        val result = retryTemplate.execute(
            operationName = "CreatePackage"
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
        val result = retryTemplate.execute(
            operationName = "UpdatePackage"
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
        val result = retryTemplate.execute(
            operationName = "DeletePackage"
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
