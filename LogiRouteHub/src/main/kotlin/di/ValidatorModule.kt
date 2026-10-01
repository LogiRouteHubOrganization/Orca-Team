package di

import domain.validator.logisticspackage.CreatePackageValidator
import domain.validator.logisticspackage.UpdatePackageValidator
import domain.validator.route.CreateRouteValidator
import domain.validator.route.UpdateRouteValidator
import domain.validator.vehicle.UpdateVehicleValidator
import domain.validator.warehouse.CreateWarehouseValidator
import domain.validator.warehouse.UpdateWarehouseValidator
import domain.validator.rule.DifferentLocationsValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator

import org.koin.dsl.module

val validatorModule = module {

    // Package Validators
    single { CreatePackageValidator() }
    single { UpdatePackageValidator() }

    // Route Validators
    single { CreateRouteValidator() }
    single { UpdateRouteValidator() }

    // Vehicle Validators
    single { UpdateVehicleValidator() }

    // Warehouse Validators
    single { CreateWarehouseValidator() }
    single { UpdateWarehouseValidator() }

    // Shared / Rule Validators (General Stateless Rules)
    single { LatitudeValidator() }
    single { LongitudeValidator() }
    single { DifferentLocationsValidator<String>() }
}