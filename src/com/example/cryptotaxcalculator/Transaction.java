package com.example.cryptotaxcalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements Comparable<Transaction> {
    public final LocalDateTime DATE;
    public final String COIN;
    public final BigDecimal AMOUNT, PRICE, TOTAL;

    public Transaction(LocalDateTime date, String coin, BigDecimal amount,
                       BigDecimal price, BigDecimal total) {
        DATE = date;
        COIN = coin;
        AMOUNT = amount;
        PRICE = price;
        TOTAL = total;
    }

    @Override
    public int compareTo(Transaction other) {
        return DATE.compareTo(other.DATE);
    }
}
