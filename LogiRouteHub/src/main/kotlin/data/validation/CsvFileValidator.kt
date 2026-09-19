package data.validation

import data.exception.LocalDataException
import java.io.File

class CsvFileValidator {

        fun validate(file: File) {
            if (!file.exists()) {
                throw LocalDataException(
                    "CSV file does not exist: ${file.name}"
                )
            }

            if (!file.isFile) {
                throw LocalDataException(
                    "CSV path is not a file: ${file.name}"
                )
            }

            if (file.length() == 0L) {
                throw LocalDataException(
                    "CSV file is empty: ${file.name}"
                )
            }
        }
    }
