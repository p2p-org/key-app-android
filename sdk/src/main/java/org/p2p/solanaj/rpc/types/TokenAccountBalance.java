package org.p2p.solanaj.rpc.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.squareup.moshi.Json;

import org.p2p.solanaj.rpc.types.RpcResultObject;

public class TokenAccountBalance extends RpcResultObject {

    public static class Balance {
        @Json(name = "amount")
        private String amount;
        @Json(name = "decimals")
        private int decimals;

        public String getAmount() {
            return amount;
        }

    }

    @Json(name = "value")
    private Balance value;

    public String getAmountString() {
        return value.getAmount();
    }

    public BigInteger getAmount() {
        return new BigInteger(value.getAmount());
    }

}