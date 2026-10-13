
package domain.extension

import kotlinx.coroutines.CancellationException

suspend fun <R> runCatchingCancellable(
    block: suspend () -> R
): Result<R> {
    return try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
