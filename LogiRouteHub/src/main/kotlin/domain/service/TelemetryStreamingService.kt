package domain.service

import domain.model.VehicleTelemetry
import kotlinx.coroutines.flow.Flow

interface TelemetryStreamingService {
    fun streamTelemetry(): Flow<VehicleTelemetry>
}