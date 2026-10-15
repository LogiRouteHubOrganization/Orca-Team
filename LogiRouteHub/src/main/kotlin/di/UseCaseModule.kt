
package di

import domain.algorithm.greedy.VehicleCoverageMapper
import domain.algorithm.knn.KNNClassifier
import domain.model.request.PackagePriorityLevels
import domain.model.request.RouteRiskConfig
import domain.pricing.decorator.PackageDecoratorComposer
import domain.tree.hierarchy.HubLineageTracer
import domain.tree.simulation.BalancedBST
import domain.tree.simulation.SequentialTrackingIdGenerator
import domain.tree.simulation.UnbalancedBST
import domain.usecase.AddVehicleToHubUseCase
import domain.usecase.AnalyzePackageDestinationDemandUseCase
import domain.usecase.AnalyzeTreePerformanceUseCase
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.CalculatePricingUseCase
import domain.usecase.CheckWarehouseConnectivityUseCase
import domain.usecase.ClassifyPackageSectorUseCase
import domain.usecase.DetectPotentialDeliveryBottleneckUseCase
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.EstimateDeliveryTimeUseCase
import domain.usecase.EstimateRouteRiskUseCase
import domain.usecase.FindFewestHopsRouteUseCase
import domain.usecase.FindOptimalPathUseCase
import domain.usecase.FindStationedVehiclesByCapacityUseCase
import domain.usecase.GetGreedyDispatchDataUseCase
import domain.usecase.GetPackageJourneyUseCase
import domain.usecase.GetWarehouseLoadFactorUseCase
import domain.usecase.PrioritizeDispatchQueueUseCase
import domain.usecase.RecommendVehicleByCapacityAndCostUseCase
import domain.usecase.ReroutePackageUseCase
import domain.usecase.SortingCargoQueueUseCase
import domain.usecase.TraceHubLineageUseCase
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
import domain.usecase.crud.warehouse.CreateWarehouseUseCase
import domain.usecase.crud.warehouse.DeleteWarehouseUseCase
import domain.usecase.crud.warehouse.GetWarehouseByIdUseCase
import domain.usecase.crud.warehouse.UpdateWarehouseUseCase
import domain.usecase.OptimizeCargoByWeightUseCase
import domain.usecase.OptimizeCargoByWeightAndVolumeUseCase
import org.koin.dsl.module

val useCaseModule = module {

    single<PackageDecoratorComposer> {
        PackageDecoratorComposer()
    }

    factory {
        CalculatePricingUseCase(
            packageDecoratorComposer = get()
        )
    }

    factory {
        SortingCargoQueueUseCase()
    }

    factory {
        AssignPackageToCargoQueueUseCase(
            sortCargoQueue = get(),
        )
    }

    factory {
        GetPackageJourneyUseCase(
            packageRepository = get(),
            findOptimalPathUseCase = get()
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

    // Package CRUD
    factory {
        CreatePackageUseCase(
            packageRepository = get()
        )
    }

    factory {
        GetPackageByIdUseCase(
            packageRepository = get()
        )
    }

    factory {
        DeletePackageUseCase(
            packageRepository = get()
        )
    }

    factory {
        UpdatePackageUseCase(
            packageRepository = get(),
            packageValidator = get()
        )
    }

    // Routing
    factory {
        FindFewestHopsRouteUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        FindOptimalPathUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        CheckWarehouseConnectivityUseCase(
            warehouseRepository = get(),
            findFewestHopsRouteUseCase = get()
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
        EstimateRouteRiskUseCase(
            riskConfig = get()
        )
    }

    // Route CRUD
    factory {
        CreateRouteUseCase(
            routeRepository = get()
        )
    }

    factory {
        DeleteRouteUseCase(
            routeRepository = get()
        )
    }

    factory {
        GetRouteByIdUseCase(
            routeRepository = get()
        )
    }

    factory {
        UpdateRouteUseCase(
            routeRepository = get(),
            routeValidator = get()
        )
    }

// Cargo Optimization - Template Method
    factory {
        OptimizeCargoByWeightUseCase()
    }

    factory {
        OptimizeCargoByWeightAndVolumeUseCase()
    }

// Vehicle
    factory {
        FindStationedVehiclesByCapacityUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        DispatchVehicleUseCase(
            vehicleRepository = get(),
            warehouseRepository = get(),
            optimizeCargoByWeightUseCase = get()
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

    // Vehicle CRUD
    factory {
        CreateVehicleUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        DeleteVehicleUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        GetVehicleByIdUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        UpdateVehicleUseCase(
            vehicleRepository = get(),
            vehicleValidator = get()
        )
    }

    // Tree simulation
    single<SequentialTrackingIdGenerator> {
        SequentialTrackingIdGenerator()
    }

    single<UnbalancedBST> {
        UnbalancedBST()
    }

    single<BalancedBST> {
        BalancedBST()
    }

    factory {
        AnalyzeTreePerformanceUseCase(
            generator = get(),
            unbalancedBST = get(),
            balancedBST = get()
        )
    }

    // Greedy dispatch
    factory<VehicleCoverageMapper> {
        VehicleCoverageMapper()
    }

    factory {
        GetGreedyDispatchDataUseCase(
            vehicleRepository = get(),
            routeRepository = get(),
            greedyInputPreparer = get()
        )
    }

    factory<PackagePriorityLevels> {
        PackagePriorityLevels()
    }

    factory {
        PrioritizeDispatchQueueUseCase(
            warehouseRepository = get(),
            priorityLevels = get()
        )
    }

    // Warehouse
    factory {
        GetWarehouseLoadFactorUseCase(
            warehouseRepository = get()
        )
    }

    single<HubLineageTracer> {
        HubLineageTracer()
    }

    factory {
        TraceHubLineageUseCase(
            hubLineageTracer = get()
        )
    }

    factory {
        DetectPotentialDeliveryBottleneckUseCase(
            warehouseRepository = get()
        )
    }

    // Warehouse CRUD
    factory {
        CreateWarehouseUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        DeleteWarehouseUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        GetWarehouseByIdUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        UpdateWarehouseUseCase(
            warehouseRepository = get(),
            warehouseValidator = get()
        )
    }

    factory { KNNClassifier() }

    factory { ClassifyPackageSectorUseCase(packageRepository = get(), knnClassifier = get()) }
}
