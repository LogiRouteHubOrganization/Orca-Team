package data.remote.retry

import data.exception.remote.NetworkException
import domain.exception.EntityValidationException

fun shouldRetry(exception: Throwable): Boolean {
    return when (exception) {
        is NetworkException.NoInternetException -> true
        is NetworkException.ServerErrorException -> true

        is EntityValidationException -> false
        is NetworkException.UnauthorizedException -> false
        is NetworkException.NotFoundException -> false
        is NetworkException.UnprocessableEntityException -> false
        is NetworkException.UnknownNetworkException -> false

        else -> false
    }
}
