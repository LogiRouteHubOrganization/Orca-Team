package data.extension

import kotlinx.coroutines.CancellationException

suspend fun <R> runCatchingCancellable(
    block: suspend () -> R
): Result<R> {
    return try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}

inline fun <R, T : R> Result<T>.recoverCatchingCancellable(
    transform: (exception: Throwable) -> R
): Result<R> {
    val exception = exceptionOrNull() ?: return this

    if (exception is CancellationException) {
        throw exception
    }

    return try {
        Result.success(transform(exception))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}
