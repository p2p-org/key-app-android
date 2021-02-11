package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;

import java.math.BigInteger;

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

    public BigInteger getAmount() {
        return new BigInteger(value.getAmount());
    }

}