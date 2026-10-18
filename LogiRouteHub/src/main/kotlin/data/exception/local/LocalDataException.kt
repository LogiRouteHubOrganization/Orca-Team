package data.exception.local

class LocalDataException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)