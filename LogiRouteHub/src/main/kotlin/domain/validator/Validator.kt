package domain.validator

interface Validator<T> {
    fun validate(value: T): ResultValidator
}
