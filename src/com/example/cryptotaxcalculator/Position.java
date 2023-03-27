package com.example.cryptotaxcalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Position {
    public final String asset;
    private BigDecimal amount;
    private BigDecimal avgPrice;
    private BigDecimal realProfit;
    private BigDecimal value;
    private LocalDateTime valueLastUpdated;

    public Position(String asset) {
        this.asset = asset;
        amount = BigDecimal.ZERO;
        avgPrice = BigDecimal.ZERO;
        value = null;
        realProfit = BigDecimal.ZERO;
    }

    public void buy(BigDecimal amount, BigDecimal price) {
        BigDecimal newAmount = this.amount.add(amount);
        avgPrice = (avgPrice.multiply(this.amount)).add(amount.multiply(price)).divide(newAmount, 10, RoundingMode.HALF_UP);
        this.amount = newAmount;
    }

    public void sell(BigDecimal amount, BigDecimal price) {
        this.amount = this.amount.subtract(amount);
        realProfit = realProfit.add(price.subtract(avgPrice).multiply(amount));
    }

    public void burn(BigDecimal amount) {
        this.amount = this.amount.subtract(amount);
    }

    public void updateValue(LocalDateTime date, BigDecimal price) {
        valueLastUpdated = date;
        value = amount.multiply(price);
    }

    @Override
    public String toString() {
        String str = asset + "\nAmount: " + amount.setScale(3, RoundingMode.HALF_UP) +
                "\nValue (as of " + valueLastUpdated + "): ";

        if (value == null) {
            str += "unknown";
        } else {
            str += value.setScale(3, RoundingMode.HALF_UP);
        }

        str += "\nAverage buy price: " + avgPrice.setScale(3, RoundingMode.HALF_UP)
                + "\nRealised profit/loss: " + realProfit.setScale(3, RoundingMode.HALF_UP);

        return str;
    }
}
