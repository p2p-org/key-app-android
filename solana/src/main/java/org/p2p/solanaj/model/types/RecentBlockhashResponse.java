package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RecentBlockhashResponse extends RpcResultObject {
    public static class FeeCalculator {

        @SerializedName("lamportsPerSignature")
        private long lamportsPerSignature;

        public long getLamportsPerSignature() {
            return lamportsPerSignature;
        }

    }

    public static class Value {
        @SerializedName("blockhash")
        private String blockhash;
        @SerializedName("feeCalculator")
        private FeeCalculator feeCalculator;

        public String getBlockhash() {
            return blockhash;
        }

        public FeeCalculator getFeeCalculator() {
            return feeCalculator;
        }

    }

    @SerializedName("value")
    private Value value;

    public Value getValue() {
        return value;
    }

    public String getRecentBlockhash() {
        return getValue().getBlockhash();
    }

    public FeeCalculator getFeeBlockhash() {
        return getValue().getFeeCalculator();
    }
}