package data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

object SupabaseClientProvider {

    private val localProperties = Properties().apply {
        val file = File("local.properties")

        if (file.exists()) {
            file.inputStream().use {
                load(it)
            }
        }
    }

    private val supabaseUrl =
        localProperties.getProperty("SUPABASE_URL")
            ?: error("SUPABASE_URL is not defined in local.properties")

    private val supabaseKey =
        localProperties.getProperty("SUPABASE_PUBLISHABLE_KEY")
            ?: error("SUPABASE_PUBLISHABLE_KEY is not defined in local.properties")

    val client = createSupabaseClient(
        supabaseUrl = supabaseUrl,
        supabaseKey = supabaseKey
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
}
