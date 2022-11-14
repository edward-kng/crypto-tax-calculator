package cryptotaxcalculator.readers;

import cryptotaxcalculator.Reader;
import cryptotaxcalculator.InvalidFileFormatException;
import cryptotaxcalculator.Transaction;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.HashMap;

public class NbxReader extends Reader {
    public NbxReader() {
        super("NBX");
    }

    @Override
    public void read(File file) throws FileNotFoundException,
            InvalidFileFormatException {
        try (Scanner sc = new Scanner(file)) {
            String[] header = sc.nextLine().replace("\"", "").split(";");

            String[] requiredColumns = {"Side", "Price", "Amount", "Total",
                    "Filled At"};
            HashMap<String, Integer> columnNrs = getColumnNrs(file,
                    header, requiredColumns);

            while (sc.hasNextLine()) {
                String[] data = sc.nextLine().replace("\"", "").split(";");
                String coin = data[columnNrs.get("Amount")].split(" ")[1];
                BigDecimal amount = new BigDecimal(data[columnNrs.get(
                        "Amount")].replace(",", "").split(" ")[0]);
                BigDecimal total = new BigDecimal(data[columnNrs.get(
                        "Total")].replace(",", "").split(" ")[0]);
                BigDecimal price = new BigDecimal(data[columnNrs.get(
                        "Price")].replace(",", "").split(" ")[0]);
                LocalDateTime date = LocalDateTime.parse(data[columnNrs.get(
                        "Filled At")],
                        DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm:ss"));

                if (data[columnNrs.get("Side")].equals("BUY")) {
                    transactionList.add(new Transaction(date, coin, amount,
                            price, total.negate()));
                } else if (data[columnNrs.get("Side")].equals("SELL")) {
                    transactionList.add(new Transaction(date, coin,
                            amount.negate(), price, total));
                }
            }
        }
    }
}
