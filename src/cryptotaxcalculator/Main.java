package cryptotaxcalculator;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import cryptotaxcalculator.readers.*;
import java.math.BigDecimal;
import java.io.PrintWriter;

public class Main {
    // TODO: Allow user to change fiat currency
    public static final String FIAT = "NOK";

    public static void main(String[] args) {
        ArrayList<Reader> readers = new ArrayList<>();
        PriorityQueue<Transaction> transactionList = new PriorityQueue<>();

        readers.add(new NbxReader());
        readers.add(new FiriReader());

        for (Reader reader : readers) {
            File dir = new File(reader.NAME);
            
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        System.out.println("Please add your CSV-files " +
                "in their respective folders, if you haven't already");

        Scanner input = new Scanner(System.in);
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
            File dir = new File(reader.NAME);

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

        for (int i = 0;i < nrTransactions && !(endDate != null
                && transactionList.peek().DATE.compareTo(endDate) > 0); i++) {
            Transaction transaction = transactionList.remove();
            BigDecimal spotPrice = transaction.PRICE;
            BigDecimal realPrice = transaction.TOTAL.divide(transaction.AMOUNT,
                    10, RoundingMode.HALF_UP).abs();

            if (!portfolio.containsKey(transaction.COIN)) {
                portfolio.put(transaction.COIN,
                        new Position(transaction.COIN));
            }

            if (transaction.AMOUNT.compareTo(BigDecimal.ZERO) < 0) {
                if (startDate == null
                        || transaction.DATE.compareTo(startDate) >= 0) {
                    portfolio.get(transaction.COIN).sell(
                            transaction.AMOUNT.abs(), realPrice);
                } else {
                    portfolio.get(transaction.COIN).burn(
                            transaction.AMOUNT.abs());
                }
            } else if (transaction.AMOUNT.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.get(transaction.COIN).buy(
                        transaction.AMOUNT, realPrice);
            }

            if (spotPrice != null) {
                portfolio.get(transaction.COIN).updateValue(transaction.DATE,
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
