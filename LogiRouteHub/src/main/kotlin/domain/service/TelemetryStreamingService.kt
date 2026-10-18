package domain.service

import domain.model.request.VehicleTelemetry
import kotlinx.coroutines.flow.Flow

interface TelemetryStreamingService {
    fun streamTelemetry(): Flow<VehicleTelemetry>
}
