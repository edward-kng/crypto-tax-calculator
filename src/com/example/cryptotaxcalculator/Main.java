package com.example.cryptotaxcalculator;

import com.example.cryptotaxcalculator.readers.CoinbaseReader;
import com.example.cryptotaxcalculator.readers.FiriReader;
import com.example.cryptotaxcalculator.readers.NbxReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ArrayList<Reader> readers = new ArrayList<>();
        PriorityQueue<Transaction> transactionList = new PriorityQueue<>();

        Scanner input = new Scanner(System.in);

        System.out.print("Fiat currency (default: USD): ");
        String fiat = input.nextLine();

        if (fiat.equals("")) {
            fiat = "USD";
        }

        readers.add(new NbxReader(fiat));
        readers.add(new FiriReader(fiat));
        readers.add(new CoinbaseReader(fiat));

        for (Reader reader : readers) {
            File dir = new File(reader.name);
            
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        System.out.println("Please add your CSV-files " +
                "in their respective folders, if you haven't already");

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        System.out.print("Start date for tax report yyyy-mm-dd (optional): ");
        String inpDate = input.nextLine();

        while (!inpDate.equals("") && startDate == null) {
            try {
                startDate = LocalDateTime.parse(inpDate + "T00:00:00");
            } catch (Exception e) {
                System.out.println("Error: Invalid date");
                System.out.print("Start date for tax report yyyy-mm-dd (optional): ");
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
                System.out.print("End date for tax report yyyy-mm-dd (optional): ");
                inpDate = input.nextLine();
            }
        }

        input.close();

        for (Reader reader : readers) {
            File dir = new File(reader.name);

            for (File file : dir.listFiles()) {
                try {
                    reader.read(file);
                } catch (FileNotFoundException e) {
                    System.err.println("Error: could not read "
                            + file.getName());
                } catch (InvalidFileFormatException e) {
                    System.err.println(e.getMessage());
                }
            }

            transactionList.addAll(reader.getTransactions());
        }

        HashMap<String, Position> portfolio = new HashMap<>();
        int nrTransactions = transactionList.size();

        for (int i = 0; i < nrTransactions && !(endDate != null
                && transactionList.peek().date.compareTo(endDate) > 0); i++) {
            Transaction transaction = transactionList.remove();
            BigDecimal spotPrice = transaction.price;
            BigDecimal realPrice = transaction.total.divide(transaction.amount,
                    10, RoundingMode.HALF_UP).abs();

            if (!portfolio.containsKey(transaction.coin)) {
                portfolio.put(transaction.coin,
                        new Position(transaction.coin));
            }

            if (transaction.amount.compareTo(BigDecimal.ZERO) < 0) {
                if (startDate == null
                        || transaction.date.compareTo(startDate) >= 0) {
                    portfolio.get(transaction.coin).sell(
                            transaction.amount.abs(), realPrice);
                } else {
                    portfolio.get(transaction.coin).burn(
                            transaction.amount.abs());
                }
            } else if (transaction.amount.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.get(transaction.coin).buy(
                        transaction.amount, realPrice);
            }

            if (spotPrice != null) {
                portfolio.get(transaction.coin).updateValue(transaction.date,
                        spotPrice);
            }
        }

        PrintWriter pw;

        try {
            pw = new PrintWriter("report.txt");
        } catch (FileNotFoundException e) {
            System.err.println("Error: could not write to file report.txt");
            System.exit(2);
            return;
        }

        pw.println("### Crypto Tax Report ###");

        for (String coin : portfolio.keySet()) {
            pw.println("\n" + portfolio.get(coin));
        }

        pw.close();
    }
}
