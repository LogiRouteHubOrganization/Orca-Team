package repository

import domain.model.Package

interface PackageRepository {

    //Fetches the complete list of packages loaded from the storage.

    fun fetchAllPackages(): List<Package>

    //Finds a specific package by its unique identifier.

    fun findPackageById(id: String): Package?
}