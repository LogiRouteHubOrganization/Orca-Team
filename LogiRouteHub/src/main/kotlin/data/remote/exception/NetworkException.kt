package data.remote.exception

sealed class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    class Unauthorized(
        message: String = "Unauthorized access (401). Please check your API key or credentials."
    ) : NetworkException(message)

    class NotFound(
        message: String = "The requested resource was not found (404)."
    ) : NetworkException(message)

    class ServerError(
        message: String = "Internal server error occurred (500). Please try again later."
    ) : NetworkException(message)

    class NoInternet(
        message: String = "No internet connection available. Please check your network settings."
    ) : NetworkException(message)

    class Unknown(
        message: String = "An unexpected network error occurred.",
        cause: Throwable? = null
    ) : NetworkException(message, cause)
}
