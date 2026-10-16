package domain.validator

sealed class ErrorValidator {

    data object DifferentLocations : ErrorValidator()

    data object BlankId : ErrorValidator()

    data class InvalidIdPrefix(
        val allowedPrefixes: Set<String>
    ) : ErrorValidator()

    data object InvalidIdFormat : ErrorValidator()

    data object InvalidLatitude : ErrorValidator()

    data object InvalidLongitude : ErrorValidator()

    data object BlankValue : ErrorValidator()

    data object MustBeNonNegative : ErrorValidator()

    data object MustBePositive : ErrorValidator()

    data object BlankPriority : ErrorValidator()

    data class InvalidPriority(
        val allowedPriorities: Set<String>
    ) : ErrorValidator()

    data object EmptyUpdate : ErrorValidator()

    data object InvalidFuelPercentage : ErrorValidator()
}
