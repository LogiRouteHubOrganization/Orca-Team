package domain.usecase.crud.logisticspackage

import domain.repository.PackageRepository
import domain.model.Package

class GetPackageByIdUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(id : String) : Package?{
        return packageRepository.findById(id)
    }
}
