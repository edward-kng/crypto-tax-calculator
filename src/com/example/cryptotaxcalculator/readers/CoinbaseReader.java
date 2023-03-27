package com.example.cryptotaxcalculator.readers;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Scanner;

import com.example.cryptotaxcalculator.InvalidFileFormatException;
import com.example.cryptotaxcalculator.Reader;
import com.example.cryptotaxcalculator.Transaction;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class CoinbaseReader extends Reader {
    public CoinbaseReader(String fiat) {
        super("Coinbase", fiat);
    }
    
    @Override
    public void read(File file)
            throws FileNotFoundException, InvalidFileFormatException {
        try (Scanner sc = new Scanner(file)) {

            try {
                for (int i = 0; i < 7; i++) {
                    sc.nextLine();
                }
            } catch (Exception e) {
                throw new InvalidFileFormatException(file, this.NAME);
            }

            String[] columns = sc.nextLine().split(",");

            String[] requiredColumns = {
                    "Timestamp", "Transaction Type", "Asset",
                    "Quantity Transacted", "Spot Price at Transaction",
                    "Total (inclusive of fees and/or spread)", "Notes"};

            HashMap<String, Integer> columnNrs = getColumnNrs(
                    file, columns, requiredColumns);

            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(",");
                LocalDateTime date = LocalDateTime.parse(
                        data[columnNrs.get("Timestamp")], DateTimeFormatter.ISO_DATE_TIME);
                String coin = data[columnNrs.get("Asset")];
                String transactionType = data[columnNrs.get("Transaction Type")];
                BigDecimal amount = new BigDecimal(
                        data[columnNrs.get("Quantity Transacted")]);
                BigDecimal price = new BigDecimal(
                        data[columnNrs.get("Spot Price at Transaction")]);

                if (transactionType.equals("Buy")) {
                    BigDecimal value = new BigDecimal(data[columnNrs.get(
                            "Total (inclusive of fees and/or spread)")]);
                    transactionList.add(new Transaction(date, coin, amount,
                                                        price, value.negate()));
                } else if (transactionType.equals("Sell")) {
                    BigDecimal value = new BigDecimal(data[columnNrs.get(
                            "Total (inclusive of fees and/or spread)")]);
                    transactionList.add(new Transaction(date, coin,
                                                        amount.negate(), price, value));
                } else if (transactionType.equals("Learning Reward")) {
                    transactionList.add(new Transaction(date, coin,
                                                        amount, price, BigDecimal.ZERO));
                } else if (transactionType.equals("Convert")) {
                    BigDecimal value = new BigDecimal(data[columnNrs.get(
                            "Total (inclusive of fees and/or spread)")]);
                    transactionList.add(new Transaction(date, coin,
                                                        amount.negate(), price, value));
                    String[] convertData = data[columnNrs.get(
                            "Notes")].split(" ");
                    transactionList.add(new Transaction(date, convertData[5],
                                        new BigDecimal(convertData[4]), null, value.negate()));
                }

                String[] notesData = data[columnNrs.get("Notes")].split(" ");

                if (transactionType.equals("Receive") && notesData[5].equals("Referral")) {
                    transactionList.add(new Transaction(date, coin,
                                                        amount, price, BigDecimal.ZERO));
                }
            }
        }
    }

    public String getName() {
        return NAME;
    }
}
