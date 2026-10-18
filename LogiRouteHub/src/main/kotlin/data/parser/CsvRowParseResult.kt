package data.parser

sealed interface CsvRowParseResult<out T> {

    data class Success<T>(
        val value: T
    ) : CsvRowParseResult<T>

    data class Failure(
        val reason: String
    ) : CsvRowParseResult<Nothing>
}