package com.example.cryptotaxcalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Calculator {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final List<Reader> readers;
    private final PriorityQueue<Transaction> transactions;
    private HashMap<String, Position> portfolio;

    public Calculator(
            LocalDateTime startDate, LocalDateTime endDate,
            List<Reader> readers) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.readers = readers;
        transactions = new PriorityQueue<>();

        readTransactions();
        calculateTransactions();
    }

    private void readTransactions() {
        for (Reader reader : readers) {
            File dir = new File(reader.name);

            for (File file : dir.listFiles()) {
                try {
                    reader.read(file);
                } catch (FileNotFoundException e) {
                    System.err.println(
                            "Error: could not read " + file.getName()
                    );
                } catch (InvalidFileFormatException e) {
                    System.err.println(e.getMessage());
                }
            }

            transactions.addAll(reader.getTransactions());
        }
    }

    private void calculateTransactions() {
        portfolio = new HashMap<>();
        int nrTransactions = transactions.size();

        for (int i = 0; i < nrTransactions && !(endDate != null
                && transactions.peek().date.compareTo(endDate) > 0); i++) {
            Transaction transaction = transactions.remove();
            BigDecimal spotPrice = transaction.price;
            BigDecimal realPrice = transaction.total.divide(
                    transaction.amount, 10, RoundingMode.HALF_UP
            ).abs();

            if (!portfolio.containsKey(transaction.coin)) {
                portfolio.put(transaction.coin, new Position(transaction.coin));
            }

            if (transaction.amount.compareTo(BigDecimal.ZERO) < 0) {
                if (startDate == null
                        || transaction.date.compareTo(startDate) >= 0) {
                    portfolio.get(transaction.coin)
                            .sell(transaction.amount.abs(), realPrice);
                } else {
                    portfolio.get(transaction.coin)
                            .burn(transaction.amount.abs());
                }
            } else if (transaction.amount.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.get(transaction.coin)
                        .buy(transaction.amount, realPrice);
            }

            if (spotPrice != null) {
                portfolio.get(transaction.coin)
                        .updateValue(transaction.date, spotPrice);
            }
        }
    }

    public void writeReport() {
        try(PrintWriter pw = new PrintWriter("report.txt")) {
            pw.println("### Crypto Tax Report ###");

            for (String coin : portfolio.keySet()) {
                pw.println("\n" + portfolio.get(coin));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: could not write to file report.txt");
            System.exit(1);
        }
    }
}
