package domain.validator

sealed class ValidationError {

    data object DifferentLocations : ValidationError()

    data object BlankId : ValidationError()

    data class InvalidIdPrefix(
        val allowedPrefixes: Set<String>
    ) : ValidationError()

    data object InvalidIdFormat : ValidationError()

    data object InvalidLatitude : ValidationError()

    data object InvalidLongitude : ValidationError()

    data object BlankValue : ValidationError()

    data object MustBeNonNegative : ValidationError()

    data object MustBePositive : ValidationError()

    data object BlankPriority : ValidationError()

    data class InvalidPriority(
        val allowedPriorities: Set<String>
    ) : ValidationError()

    data object EmptyUpdate : ValidationError()
}
