package data.remote.datasource.supabase

import data.remote.SupabaseClientProvider
import data.remote.datasource.PackageRemoteDataSource
import data.remote.dto.PackageDto
import data.remote.dto.request.PackageRequestDto
import data.remote.utils.safeApiCall
import io.github.jan.supabase.postgrest.from

class PackageRemoteDataSourceImpl : PackageRemoteDataSource {

    private val client = SupabaseClientProvider.client
    private val tableName = "packages"

    override suspend fun getAllPackages(): List<PackageDto> {
        return safeApiCall {
            client.from(tableName)
                .select()
                .decodeList<PackageDto>()
        }
    }

    override suspend fun getPackageById(id: String): PackageDto? {
        return safeApiCall {
            client.from(tableName)
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<PackageDto>()
        }
    }

    override suspend fun createPackage(packageRequest: PackageRequestDto): PackageDto {
        return safeApiCall {
            client.from(tableName)
                .insert(packageRequest) {
                    select()
                }
                .decodeSingle<PackageDto>()
        }
    }

    override suspend fun updatePackage(id: String, packageRequest: PackageRequestDto): PackageDto {
        return safeApiCall {
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

    override suspend fun deletePackage(id: String) {
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
