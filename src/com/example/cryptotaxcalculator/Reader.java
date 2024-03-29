package com.example.cryptotaxcalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class Reader {
    public final String name;
    protected final String fiat;
    protected ArrayList<Transaction> transactionList;

    public Reader(String name, String fiat) {
        this.name = name;
        this.fiat = fiat;
        transactionList = new ArrayList<>();
    }
    public abstract void read(File file)
            throws FileNotFoundException, InvalidFileFormatException;

    public ArrayList<Transaction> getTransactions() {
        return transactionList;
    }

    protected HashMap<String, Integer> getColumnNrs(
            File file, String[] header, String[] requiredColumns
    ) throws InvalidFileFormatException {
        List<String> columns = Arrays.asList(header);
        HashMap<String, Integer> columnNrs = new HashMap<>();

        for (String column : requiredColumns) {
            int i = columns.indexOf(column);

            if (i == -1) {
                throw new InvalidFileFormatException(file, name);
            } else {
                columnNrs.put(column, i);
            }
        }

        return columnNrs;
    }
}