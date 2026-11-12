package com.github.yashwant3.reconengine.matching;

import com.github.yashwant3.reconengine.model.Break;
import com.github.yashwant3.reconengine.model.Trade;

public class BreakDetector {

    public static Break detect(Trade bank, Trade clearing){
        boolean priceMismatch= bank.price().compareTo(clearing.price()) != 0;
        boolean quantityMismatch= bank.quantity() != clearing.quantity();

        if(!priceMismatch && !quantityMismatch){
            return null; // clean null
        }

        if(priceMismatch){
            return new Break(
                    bank.tradeId(),
                    Break.BreakType.PRICE_MISMATCH,
                    bank.price(),
                    clearing.price(),
                    bank.quantity(),
                    clearing.quantity()
            );
        }
        return new Break(
                bank.tradeId(),
                Break.BreakType.QUANTITY_MISMATCH,
                bank.price(),
                clearing.price(),
                bank.quantity(),
                clearing.quantity()
        );
    }
}
