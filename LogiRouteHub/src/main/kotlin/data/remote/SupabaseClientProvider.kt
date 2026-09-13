package data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

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
        install(Postgrest)
    }
}
