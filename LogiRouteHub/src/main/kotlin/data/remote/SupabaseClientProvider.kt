package data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseClientProvider {

    private val supabaseUrl =
        System.getenv("SUPABASE_URL")
            ?: error("SUPABASE_URL environment variable is not set")

    private val supabaseKey =
        System.getenv("SUPABASE_PUBLISHABLE_KEY")
            ?: error("SUPABASE_PUBLISHABLE_KEY environment variable is not set")

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
