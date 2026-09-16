package com.github.yashwant3.reconengine.model;

import java.math.BigDecimal;

public record Break(String tradeId,
                    BreakType type,
                    BigDecimal bankPrice,
                    BigDecimal clearingPrice,
                    Integer bankQuantity,
                    Integer clearingQuantity
) {
    public enum BreakType{
        MISSING_IN_CLEARING,
        MISSING_IN_BANK,
        PRICE_MISMATCH,
        QUANTITY_MISMATCH
    }
}
