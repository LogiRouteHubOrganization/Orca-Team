
package di

import data.remote.ratelimit.RateLimiter
import data.remote.retry.RetryableRemoteDatasource
import data.remote.circuitbreaker.CircuitBreaker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.File
import java.util.Properties

val networkModule = module {
    single<SupabaseClient> {
        val supabaseSpecs = loadSupabaseCredentials()
        createSupabaseClient(supabaseSpecs)
    }
    single {
        CircuitBreaker()
    }

    single {
        RetryableRemoteDatasource()
    }

    single {
        RateLimiter(
            maxBucketCapacity = 10,
            refillRatePerSecond = 5.0
        )
    }
}

private fun createSupabaseClient(
    supabaseCredentials: SupabaseCredentials
): SupabaseClient =
    createSupabaseClient(
        supabaseUrl = supabaseCredentials.supabaseUrl,
        supabaseKey = supabaseCredentials.supabaseKey
    ) {
        install(Postgrest) {
            serializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

private fun loadSupabaseCredentials(): SupabaseCredentials {
    val propertiesFile = File("local.properties")

    require(propertiesFile.exists()) {
        "local.properties file not found at: ${propertiesFile.absolutePath}"
    }

    val localProperties = Properties()

    propertiesFile.inputStream().use { inputStream ->
        localProperties.load(inputStream)
    }

    val supabaseUrl = localProperties
        .getProperty("SUPABASE_URL")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: error("SUPABASE_URL is missing or empty")

    val supabaseKey = localProperties
        .getProperty("SUPABASE_PUBLISHABLE_KEY")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: error("SUPABASE_PUBLISHABLE_KEY is missing or empty")

    require(supabaseUrl.startsWith("https://")) {
        "SUPABASE_URL must start with https://"
    }

    return SupabaseCredentials(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    )
}

private data class SupabaseCredentials(
    val supabaseUrl: String,
    val supabaseKey: String
)
