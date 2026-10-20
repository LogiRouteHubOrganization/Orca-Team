package data.extension

import data.exception.local.LocalDataException
import data.exception.local.toDomainException
import data.exception.remote.toDomainException


fun <T> Result<T>.mapLocalDataException(): Result<T> {
    return recoverCatchingCancellable { throwable ->
        throw when (throwable) {
            is LocalDataException -> throwable.toDomainException()
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
            is LocalDataException -> throwable.toDomainException()
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

