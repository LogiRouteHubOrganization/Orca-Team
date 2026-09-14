package data.remote.datasource

import data.remote.dto.PackageDto
import data.remote.dto.request.PackageRequestDto

interface PackageRemoteDataSource {
    suspend fun getAllPackages(): List<PackageDto>
    suspend fun getPackageById(id: String): PackageDto?
    suspend fun createPackage(packageRequest: PackageRequestDto): PackageDto
    suspend fun updatePackage(id: String, packageRequest: PackageRequestDto): PackageDto
    suspend fun deletePackage(id: String)
}
