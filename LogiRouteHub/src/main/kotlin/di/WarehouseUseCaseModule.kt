package di
import domain.usecase.GetWarehouseLoadFactorUseCase
import domain.usecase.TraceHubLineageUseCase
import domain.usecase.DetectPotentialDeliveryBottleneckUseCase
import domain.usecase.crud.warehouse.CreateWarehouseUseCase
import domain.usecase.crud.warehouse.DeleteWarehouseUseCase
import domain.usecase.crud.warehouse.GetWarehouseByIdUseCase
import domain.usecase.crud.warehouse.UpdateWarehouseUseCase
import org.koin.dsl.module

val useCaseModule = module {

    factory {
        GetWarehouseLoadFactorUseCase(
            warehouseRepository = get()
        )
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
}

