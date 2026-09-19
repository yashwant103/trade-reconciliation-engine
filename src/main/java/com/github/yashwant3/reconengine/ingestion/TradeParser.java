package com.github.yashwant3.reconengine.ingestion;

import com.github.yashwant3.reconengine.model.Trade;
import java.math.BigDecimal;
public class TradeParser {
    public static Trade parse(String csvLine, Trade.Source source){
        String[] parts= csvLine.split(",");
        return new Trade(
            parts[0], // tardeId
            parts[1], // accountID
            parts[2], // symbol
            parts[3], // side
            Integer.parseInt(parts[4]), // quantity
            new BigDecimal(parts[5]), // price
            Long.parseLong(parts[6]), // timestamp
            source
        );
    }
}
