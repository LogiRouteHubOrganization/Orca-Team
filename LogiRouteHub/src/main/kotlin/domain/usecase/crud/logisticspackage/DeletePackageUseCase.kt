package domain.usecase.crud.logisticspackage
import domain.repository.PackageRepository

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(id : String) {
        packageRepository.delete(id)
    }
}
