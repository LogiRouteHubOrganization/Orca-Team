package data.remote.datasource

import data.remote.dto.response.PackageDto
import data.remote.dto.request.PackageCreateRequestDto
import data.remote.dto.request.PackageUpdateRequestDto

interface PackageRemoteDataSource {

    suspend fun getAllPackages(): List<PackageDto>

    suspend fun getPackageById(id: String): PackageDto?

    suspend fun createPackage(
        packageRequest: PackageCreateRequestDto
    ): PackageDto

    suspend fun updatePackage(
        id: String,
        packageRequest: PackageUpdateRequestDto
    ): PackageDto

    suspend fun deletePackage(id: String)
}
