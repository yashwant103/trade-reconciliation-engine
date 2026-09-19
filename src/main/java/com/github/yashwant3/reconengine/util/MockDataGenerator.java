//package com.github.yashwant3.reconengine.util;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.Instant;
//import java.util.Random;
//import java.util.UUID;
//
//public class MockDataGenerator {
//    private static final String[] SYMBOLS= {"AAPL","MSFT","GOOGL","AMZN","NVDA","GS"};
//    private static final String[] SIDES={"BUY","SELL"};
//
//    public static void main(String[] args) throws Exception{
//        int totalRecords = args.length > 0 ?Integer.parseInt(args[0]) : 10_000;
//        generateSamples(totalRecords);
//    }
//    public static void generateSamples(int totalRecords) throws Exception{
//        Path dataDir = Paths.get("data");
//        if(!Files.exists(dataDir)) Files.createDirectories(dataDir);
//
//        Path bankPath = dataDir.resolve("internal_legfer_sample.csv");
//        Path clearingPath= dataDir.resolve("clearinghouse_sample.csv");
//        Random random= new Random(42);
//
//        try(BufferedWriter bankWriter = Files.newBufferedWriter(bankPath);
//        BufferedWriter clearingWriter= Files.newBufferedWriter(clearingPath)){
//            String header= "trade_id,accoundt_id,symbol,side,quantity,price,timestamp\n";
//            bankWriter.write(header);
//            clearingWriter.write(header);
//
//            for(int i=0;i< totalRecords;i++){
//                String tradeId= UUID.randomUUID().toString();
//                String accountId= "ACC-"+(1000+random.nextInt(9000));
//                String symbol= SYMBOLS[random.nextInt(SYMBOLS.length)];
//                String side= SIDES[random.nextInt(SIDES.length)];
//                int quantity= (random.nextInt(50)+1 * 10);
//                BigDecimal price= BigDecimal.valueOf(50 + random.nextDouble() * 450).setScale(2,RoundingMode.HALF_UP);
//                long timestamp= Instant.now().toEpochMilli() - random.nextInt(86400000);
//
//                int scenarioRoll= random.nextInt(100);
//
//                if(scenarioRoll < 90){
//                    //Identical row onn each sides
//
//                    writeRow(bankWriter,tradeId, accountId,symbol,side,quantity,price, timestamp);
//                    writeRow(bankWriter, tradeId,accountId,symbol,side,quantity,price,timestamp);
//
//                }else if(scenarioRoll < 95){
//                    //Price mismacth
//
//                    BigDecimal clearingPrice= price.add(BigDecimal.valueOf(0.50)).setScale(2,RoundingMode.HALF_UP);
//                    writeRow(bankWriter,tradeId, accountId,symbol, side, quantity,price, timestamp);
//                    writeRow(clearingWriter,tradeId, accountId,symbol,side,quantity,price,timestamp);
//                }else if(scenarioRoll < 99){
//                    //Quantity mismatch
//                    writeRow(bankWriter,tradeId,accountId,symbol,side,quantity,price,timestamp);
//                }else if(scenarioRoll < 97){
//                    // Missing in clearing (only on bank side)
//                    writeRow(bankWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
//                } else {
//                    // Missing in bank (only on clearing side)
//                    writeRow(clearingWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
//                }
//            }
//        }
//        System.out.println("Generated sample files inside "+ dataDir.toAbsolutePath());
//    }
//    public static void writeRow(BufferedWriter writer,String id, String acc, String sym,String side,int qty, BigDecimal price, long ts) throws Exception{
//        writer.write(String.format("%s,%s,%s,%s,%d,%s,%d\n", id, acc, sym, side, qty,price.toPlainString(),ts));
//    }
//}


package com.github.yashwant3.reconengine.util;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class MockDataGenerator {
    private static final String[] SYMBOLS = {"AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", "GS"};
    private static final String[] SIDES = {"BUY", "SELL"};

    public static void main(String[] args) throws Exception {
        int totalRecords = args.length > 0 ? Integer.parseInt(args[0]) : 10_000;
        generateSamples(totalRecords);
    }

    public static void generateSamples(int totalRecords) throws Exception {
        Path dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) Files.createDirectories(dataDir);

        Path bankPath = dataDir.resolve("internal_ledger_sample.csv");
        Path clearingPath = dataDir.resolve("clearinghouse_sample.csv");
        Random random = new Random(42);

        try (BufferedWriter bankWriter = Files.newBufferedWriter(bankPath);
             BufferedWriter clearingWriter = Files.newBufferedWriter(clearingPath)) {

            String header = "trade_id,account_id,symbol,side,quantity,price,timestamp\n";
            bankWriter.write(header);
            clearingWriter.write(header);

            for (int i = 0; i < totalRecords; i++) {
                String tradeId = UUID.randomUUID().toString();
                String accountId = "ACC-" + (1000 + random.nextInt(9000));
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                String side = SIDES[random.nextInt(SIDES.length)];
                int quantity = (random.nextInt(50) + 1) * 10;
                BigDecimal price = BigDecimal.valueOf(50 + random.nextDouble() * 450).setScale(2, RoundingMode.HALF_UP);
                long timestamp = Instant.now().toEpochMilli() - random.nextInt(86_400_000);

                int scenarioRoll = random.nextInt(100);

                if (scenarioRoll < 85) {
                    // Clean match: identical row on both sides
                    writeRow(bankWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
                    writeRow(clearingWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);

                } else if (scenarioRoll < 90) {
                    // Price mismatch
                    BigDecimal clearingPrice = price.add(BigDecimal.valueOf(0.50)).setScale(2, RoundingMode.HALF_UP);
                    writeRow(bankWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
                    writeRow(clearingWriter, tradeId, accountId, symbol, side, quantity, clearingPrice, timestamp);

                } else if (scenarioRoll < 94) {
                    // Quantity mismatch
                    int clearingQuantity = quantity + 10;
                    writeRow(bankWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
                    writeRow(clearingWriter, tradeId, accountId, symbol, side, clearingQuantity, price, timestamp);

                } else if (scenarioRoll < 97) {
                    // Missing in clearing (only on bank side)
                    writeRow(bankWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);

                } else {
                    // Missing in bank (only on clearing side)
                    writeRow(clearingWriter, tradeId, accountId, symbol, side, quantity, price, timestamp);
                }
            }
        }
        System.out.println("Generated sample files inside " + dataDir.toAbsolutePath());
    }

    public static void writeRow(BufferedWriter writer, String id, String acc, String sym,
                                String side, int qty, BigDecimal price, long ts) throws Exception {
        writer.write(String.format("%s,%s,%s,%s,%d,%s,%d%n", id, acc, sym, side, qty, price.toPlainString(), ts));
    }
}