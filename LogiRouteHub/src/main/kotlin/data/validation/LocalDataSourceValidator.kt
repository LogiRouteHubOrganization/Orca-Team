
package data.validation

import data.exception.local.LocalDataException
import java.io.File

class LocalDataSourceValidator(
    private val requiredExtension: String = "csv"
) : LocalDataValidator<File> {

    override fun validate(input: File) {
        if (!input.exists()) {
            throw LocalDataException(
                "Data file does not exist: ${input.name}"
            )
        }

        if (!input.isFile) {
            throw LocalDataException(
                "Data source path is not a file: ${input.name}"
            )
        }

        if (
            !input.extension.equals(
                requiredExtension,
                ignoreCase = true
            )
        ) {
            throw LocalDataException(
                "Invalid file format. Expected .$requiredExtension " +
                        "but got .${input.extension}"
            )
        }

        if (input.length() == 0L) {
            throw LocalDataException(
                "Data file is empty: ${input.name}"
            )
        }
    }
}
