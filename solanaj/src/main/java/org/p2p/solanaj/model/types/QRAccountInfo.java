package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class QRAccountInfo extends RpcResultObject {

    public static class Value {
        @SerializedName("data")
        private Object data;
        @SerializedName("executable")
        private boolean executable;
        @SerializedName("lamports")
        private long lamports;
        @SerializedName("owner")
        private String owner;
        @SerializedName("rentEpoch")
        private long rentEpoch;

        public Object getData() {
            return data;
        }

        public boolean isExecutable() {
            return executable;
        }

        public long getLamports() {
            return lamports;
        }

        public String getOwner() {
            return owner;
        }

        public long getRentEpoch() {
            return rentEpoch;
        }

    }

    public static class Data {
        @SerializedName("parsed")
        private Parsed parsed;

        public Parsed getParsed() {
            return parsed;
        }
    }

    public static class Parsed {
        @SerializedName("info")
        private Info info;

        public Info getInfo() {
            return info;
        }
    }

    public static class Info {
        @SerializedName("isNative")
        boolean isNative;
        @SerializedName("mint")
        String mint;
        @SerializedName("owner")
        String owner;
        @SerializedName("state")
        String state;

        public boolean isNative() {
            return isNative;
        }

        public String getMint() {
            return mint;
        }

        public String getOwner() {
            return owner;
        }

        public String getState() {
            return state;
        }
    }

    @SerializedName("value")
    private Value value;

    public Value getValue() {
        return value;
    }
}
