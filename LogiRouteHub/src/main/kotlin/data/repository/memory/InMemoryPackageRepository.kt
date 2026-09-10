package data.repository.memory

import domain.model.Package
import domain.repository.PackageRepository

class InMemoryPackageRepository(
    private val packages: MutableList<Package> = mutableListOf()
) : PackageRepository {

    override fun findById(packageId: String): Package? {
        return packages.find { it.id == packageId }
    }

    override fun findAll(): List<Package> {
        return packages
    }

    override fun add(logisticsPackage: Package) {
        packages.add(logisticsPackage)
    }

    override fun update(logisticsPackage: Package) {

        val notFoundIndex = -1
        val packageIndex = packages.indexOfFirst {
            it.id == logisticsPackage.id
        }

        if (packageIndex != notFoundIndex) {
            packages[packageIndex] = logisticsPackage
        }
    }
}
