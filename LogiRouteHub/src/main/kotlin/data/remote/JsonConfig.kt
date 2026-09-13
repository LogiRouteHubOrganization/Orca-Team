import kotlinx.serialization.json.Json

object JsonConfig {

    val instance = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

}