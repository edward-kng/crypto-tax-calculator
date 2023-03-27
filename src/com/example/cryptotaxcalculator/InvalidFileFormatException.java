package com.example.cryptotaxcalculator;

import java.io.File;

public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(File file, String formatName) {
        super(
                "Error: could not read file: " + file.getName()
                + ", not in correct format for " + formatName
        );
    }
}
