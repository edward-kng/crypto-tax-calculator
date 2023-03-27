package com.example.cryptotaxcalculator.readers;

import com.example.cryptotaxcalculator.InvalidFileFormatException;
import com.example.cryptotaxcalculator.Reader;
import com.example.cryptotaxcalculator.Transaction;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class FiriReader extends Reader {
    public FiriReader(String fiat) {
        super("Firi", fiat);
    }

    @Override
    public void read(File file) throws FileNotFoundException, InvalidFileFormatException {
        try (Scanner sc = new Scanner(file)) {
            HashMap<String, ArrayList<String[]>> matchOrders = new HashMap<>();

            String[] header = sc.nextLine().split(",");
            String[] requiredColumns = {"Match ID", "Action", "Currency", "Amount", "Created at"};

            HashMap<String, Integer> columnNrs = getColumnNrs(file, header, requiredColumns);

            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().split(",");
                String coin = data[columnNrs.get("Currency")];
                BigDecimal amount = new BigDecimal(data[columnNrs.get("Amount")]);
                LocalDateTime date = parseDate(data[columnNrs.get("Created at")]);

                if (data[columnNrs.get("Action")].contains("Bonus")
                        && !coin.equals(FIAT)) {
                    transactionList.add(
                            new Transaction(date, coin, amount, null, BigDecimal.ZERO)
                    );
                } else if (data[columnNrs.get("Action")].contains("Match")) {
                    String matchId = data[columnNrs.get("Match ID")];

                    if (!matchOrders.containsKey(matchId)) {
                        matchOrders.put(matchId, new ArrayList<>());
                    }

                    matchOrders.get(matchId).add(data);
                }
            }

            for (String match : matchOrders.keySet()) {
                BigDecimal orderValue = BigDecimal.ZERO;
                BigDecimal orderAmount = BigDecimal.ZERO;
                String orderCoin = null;
                LocalDateTime date = parseDate(matchOrders.get(match).get(0)[columnNrs.get("Created at")]);

                for (String[] transactionData : matchOrders.get(match)) {
                    String coin = transactionData[columnNrs.get("Currency")];

                    if (coin.equals(FIAT)) {
                        orderValue = orderValue.add(new BigDecimal(
                                transactionData[columnNrs.get("Amount")]));
                    } else {
                        if (orderCoin == null) {
                            orderCoin = coin;
                        }

                        orderAmount = orderAmount.add(new BigDecimal(
                                transactionData[columnNrs.get("Amount")]));
                    }
                }

                // TODO: Use spot price instead of real price
                BigDecimal price = orderValue.divide(orderAmount, 10,
                        RoundingMode.HALF_UP).abs();
                transactionList.add(new Transaction(date, orderCoin,
                        orderAmount, price, orderValue));
            }
        }
    }

    private LocalDateTime parseDate(String dateString) {
        String[] dateInfo = dateString.split(" ");
        return LocalDateTime.parse(
                dateString.replace("(Coordinated Universal Time)",
                        "").replace(dateInfo[5], "").trim(),
                DateTimeFormatter.ofPattern("EEE MMM dd uuuu HH:mm:ss",
                        Locale.ENGLISH));
    }
}
