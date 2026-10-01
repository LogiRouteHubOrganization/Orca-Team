package di

import domain.algorithm.dispatcher.VehicleCoverageMapper
import domain.model.request.PackagePriorityLevels
import domain.model.request.RouteRiskConfig
import domain.pricing.decorator.PackageDecoratorComposer
import domain.tree.simulation.BalancedBST
import domain.tree.simulation.SequentialTrackingIdGenerator
import domain.tree.simulation.UnbalancedBST
import domain.usecase.AddVehicleToHubUseCase
import domain.usecase.AnalyzePackageDestinationDemandUseCase
import domain.usecase.AnalyzeTreePerformanceUseCase
import domain.usecase.AssignPackageToCargoQueueUseCase
import org.koin.dsl.module
import domain.usecase.CalculatePricingUseCase
import domain.usecase.CheckWarehouseConnectivityUseCase
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.EstimateDeliveryTimeUseCase
import domain.usecase.EstimateRouteRiskUseCase
import domain.usecase.FindFewestHopsRouteUseCase
import domain.usecase.FindOptimalPathUseCase
import domain.usecase.FindStationedVehiclesByCapacityUseCase
import domain.usecase.GetGreedyDispatchDataUseCase
import domain.usecase.GetPackageJourneyUseCase
import domain.usecase.PrioritizeDispatchQueueUseCase
import domain.usecase.RecommendVehicleByCapacityAndCostUseCase
import domain.usecase.ReroutePackageUseCase
import domain.usecase.SortingCargoQueueUseCase
import domain.usecase.TriggerFleetRebalancingUseCase
import domain.usecase.crud.logisticspackage.CreatePackageUseCase
import domain.usecase.crud.logisticspackage.DeletePackageUseCase
import domain.usecase.crud.logisticspackage.GetPackageByIdUseCase
import domain.usecase.crud.logisticspackage.UpdatePackageUseCase
import domain.usecase.crud.route.CreateRouteUseCase
import domain.usecase.crud.route.DeleteRouteUseCase
import domain.usecase.crud.route.GetRouteByIdUseCase
import domain.usecase.crud.route.UpdateRouteUseCase
import domain.usecase.crud.vehicle.CreateVehicleUseCase
import domain.usecase.crud.vehicle.DeleteVehicleUseCase
import domain.usecase.crud.vehicle.GetVehicleByIdUseCase
import domain.usecase.crud.vehicle.UpdateVehicleUseCase


val useCaseModule = module {

    single<PackageDecoratorComposer> { PackageDecoratorComposer() }

    factory {
        CalculatePricingUseCase(packageDecoratorComposer = get())
    }

    factory {
        SortingCargoQueueUseCase()
    }

    factory {
        AssignPackageToCargoQueueUseCase(
            sortCargoQueue = get(),
            warehouseRepository = get()
        )
    }

    factory {
        GetPackageJourneyUseCase(
            packageRepository = get(),
            shortestPathRouter = get()
        )
    }

    factory {
        AnalyzePackageDestinationDemandUseCase()
    }

    factory {
        ReroutePackageUseCase(
            packageRepository = get(),
            warehouseRepository = get(),
            updatePackageValidator = get()
        )
    }

    factory {
        CreatePackageUseCase(packageRepository = get())
    }

    factory {
        GetPackageByIdUseCase(packageRepository = get())
    }

    factory {
        DeletePackageUseCase(packageRepository = get())
    }

    factory {
        UpdatePackageUseCase(
            packageRepository = get(),
            packageValidator = get()
        )
    }


    factory {
        FindFewestHopsRouteUseCase(
            warehouseRepository = get(),
            pathFinder = get()
        )
    }


    factory {
        FindOptimalPathUseCase(
            warehouseRepository = get(),
            shortestPathFinder = get()
        )
    }


    factory {
        CheckWarehouseConnectivityUseCase(
            warehouseRepository = get(),
            leastHopRouter = get()
        )
    }

    factory {
        EstimateDeliveryTimeUseCase(
            estimateRouteRiskUseCase = get()
        )
    }

    single<RouteRiskConfig> {
        RouteRiskConfig(
            highDistanceThresholdKm = 100.0,
            highDelayThresholdMin = 60,
            mediumRiskScoreThreshold = 1.0,
            highRiskScoreThreshold = 2.0
        )
    }

    factory {
        EstimateRouteRiskUseCase(riskConfig = get())
    }

    factory {
        CreateRouteUseCase(routeRepository = get())
    }

    factory {
        DeleteRouteUseCase(routeRepository = get())
    }

    factory {
        GetRouteByIdUseCase(routeRepository = get())
    }

    factory {
        UpdateRouteUseCase(
            routeRepository = get(),
            routeValidator = get()
        )
    }

    factory {
        FindStationedVehiclesByCapacityUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        DispatchVehicleUseCase(
            vehicleRepository = get(),
            warehouseRepository = get()
        )
    }

    factory {
        AddVehicleToHubUseCase(
            warehouseRepository = get(),
            vehicleRepository = get(),
            updateVehicleValidator = get()
        )
    }

    factory {
        RecommendVehicleByCapacityAndCostUseCase(
            findStationedVehiclesByCapacityUseCase = get()
        )
    }

    factory {
        TriggerFleetRebalancingUseCase(
            warehouseRepository = get(),
            getWarehouseLoadFactorUseCase = get()
        )
    }

    factory {
        CreateVehicleUseCase(vehicleRepository = get())
    }

    factory {
        DeleteVehicleUseCase(vehicleRepository = get())
    }

    factory {
        GetVehicleByIdUseCase(vehicleRepository = get())
    }

    factory {
        UpdateVehicleUseCase(
            vehicleRepository = get(),
            vehicleValidator = get()
        )
    }

    single<SequentialTrackingIdGenerator> {
        SequentialTrackingIdGenerator()
    }

    single<UnbalancedBST> { UnbalancedBST() }

    single<BalancedBST> { BalancedBST() }


    factory {
        AnalyzeTreePerformanceUseCase(
            generator = get(),
            unbalancedBST = get(),
            balancedBST = get()
        )
    }

    factory<VehicleCoverageMapper> { VehicleCoverageMapper() }

    factory {
        GetGreedyDispatchDataUseCase(
            vehicleRepository = get(),
            routeRepository = get(),
            greedyInputPreparer = get()
        )
    }

    factory<PackagePriorityLevels> { PackagePriorityLevels() }

    factory {
        PrioritizeDispatchQueueUseCase(
            warehouseRepository = get(),
            priorityLevels = get()
        )
    }


}


