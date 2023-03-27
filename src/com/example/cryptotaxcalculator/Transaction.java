package com.example.cryptotaxcalculator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements Comparable<Transaction> {
    public final LocalDateTime date;
    public final String coin;
    public final BigDecimal amount;
    public final BigDecimal price;
    public final BigDecimal total;

    public Transaction(
            LocalDateTime date, String coin, BigDecimal amount,
            BigDecimal price, BigDecimal total
    ) {
        this.date = date;
        this.coin = coin;
        this.amount = amount;
        this.price = price;
        this.total = total;
    }

    @Override
    public int compareTo(Transaction other) {
        return date.compareTo(other.date);
    }
}
