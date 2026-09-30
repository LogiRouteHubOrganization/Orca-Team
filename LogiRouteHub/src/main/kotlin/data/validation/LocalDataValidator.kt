package data.validation

interface LocalDataValidator<T> {
    fun validate(input: T)
}
