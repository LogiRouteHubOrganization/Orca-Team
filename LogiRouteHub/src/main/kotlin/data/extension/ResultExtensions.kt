package data.extension

import kotlinx.coroutines.CancellationException
import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException

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
fun <T> Result<T>.mapLocalDataException(): Result<T> {
    return recoverCatchingCancellable { throwable ->
        throw when (throwable) {
            is LocalDataException ->
                throwable.toDomainException()

            else -> throwable
        }
    }
}

fun <T> Result<T>.mapRepositoryException(
    resourceType: String,
    resourceId: String? = null
): Result<T> {
    return recoverCatchingCancellable { throwable ->
        throw when (throwable) {
            is LocalDataException ->
                throwable.toDomainException()

            else -> {
                if (resourceId != null) {
                    throwable.toDomainException(
                        resourceType = resourceType,
                        resourceId = resourceId
                    )
                } else {
                    throwable.toDomainException(
                        resourceType = resourceType
                    )
                }
            }
        }
    }
}
