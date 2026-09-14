package com.github.yashwant3.reconengine.model;

import java.math.BigDecimal;
public record Trade(String tradeId,
                    String account,
                    String symbol,
                    String side,
                    int quantity,
                    BigDecimal price,
                    long timestamp,
                    Source source
) {
    public enum Source{
        BANK,CLEARINGHOUSE
    }
}
