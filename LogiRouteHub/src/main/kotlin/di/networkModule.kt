package di

import data.remote.retry.RetryableRemoteDatasource
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
        di.createSupabaseClient(supabaseSpecs)
    }

    single {
        RetryableRemoteDatasource()
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
    val localProperties = Properties()
    val propertiesFile = File("local.properties")

    require(propertiesFile.exists()) {
        "local.properties file not found"
    }

    propertiesFile.inputStream().use { inputStream ->
        localProperties.load(inputStream)
    }

    val supabaseUrl = localProperties.getProperty("SUPABASE_URL")
        ?: error("SUPABASE_URL is not defined in local.properties")

    val supabaseKey = localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")
        ?: error("SUPABASE_PUBLISHABLE_KEY is not defined in local.properties")

    return SupabaseCredentials(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
    )
}

private data class SupabaseCredentials(
    val supabaseUrl: String,
    val supabaseKey: String
)


