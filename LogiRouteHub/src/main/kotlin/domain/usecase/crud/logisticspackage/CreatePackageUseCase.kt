package domain.usecase.crud.logisticspackage

import domain.model.Package
import domain.repository.PackageRepository

class CreatePackageUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        logisticsPackage: Package
    ): Result<Package> {
        return packageRepository.add(logisticsPackage)
    }
}

