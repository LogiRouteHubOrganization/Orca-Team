package data.exception.remote

sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class UnauthorizedException(
        message: String = "Unauthorized access (401). Please check your API key or credentials."
    ) : NetworkException(message)

    class NotFoundException(
        message: String = "The requested resource was not found (404)."
    ) : NetworkException(message)

    class ServerErrorException(
        message: String = "Internal server error occurred (500). Please try again later."
    ) : NetworkException(message)

    class NoInternetException(
        message: String = "No internet connection available. Please check your network settings."
    ) : NetworkException(message)

    class UnknownNetworkException(
        message: String = "An unexpected network error occurred.",
        cause: Throwable? = null
    ) : NetworkException(message, cause)
}
