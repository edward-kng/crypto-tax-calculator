package com.example.cryptotaxcalculator;

import com.example.cryptotaxcalculator.readers.CoinbaseReader;
import com.example.cryptotaxcalculator.readers.FiriReader;
import com.example.cryptotaxcalculator.readers.NbxReader;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class Main {
    private static List<Reader> loadReaders(String fiat) {
        List<Reader> readers = new ArrayList<>();

        readers.add(new NbxReader(fiat));
        readers.add(new FiriReader(fiat));
        readers.add(new CoinbaseReader(fiat));

        for (Reader reader : readers) {
            File dir = new File(reader.name);

            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        return readers;
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Fiat currency (default: USD): ");
        String fiat = input.nextLine();

        if (fiat.equals("")) {
            fiat = "USD";
        }

        List<Reader> readers = loadReaders(fiat);

        System.out.println(
                "Please add your CSV-files in their respective folders, if you "
                        + "haven't already"
        );

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        System.out.print("Start date for tax report yyyy-mm-dd (optional): ");
        String inpDate = input.nextLine();

        while (!inpDate.equals("") && startDate == null) {
            try {
                startDate = LocalDateTime.parse(inpDate + "T00:00:00");
            } catch (Exception e) {
                System.out.println("Error: Invalid date");
                System.out.print(
                        "Start date for tax report yyyy-mm-dd (optional): "
                );
                inpDate = input.nextLine();
            }
        }

        System.out.print("End date for tax report yyyy-mm-dd (optional): ");
        inpDate = input.nextLine();

        while (!inpDate.equals("") && endDate == null) {
            try {
                endDate = LocalDateTime.parse(inpDate + "T00:00:00");
            } catch (Exception e) {
                System.out.println("Error: invalid date");
                System.out.print(
                        "End date for tax report yyyy-mm-dd (optional): "
                );
                inpDate = input.nextLine();
            }
        }

        input.close();

        Calculator calculator = new Calculator(readers);
        calculator.calculateTransactions(startDate, endDate);
        calculator.writeReport();
    }
}
