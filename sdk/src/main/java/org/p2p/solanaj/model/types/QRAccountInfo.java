package org.p2p.solanaj.model.types;

import com.squareup.moshi.Json;

public class QRAccountInfo extends RpcResultObject {

    public static class Value {
        @Json(name = "data")
        private Object data;
        @Json(name = "executable")
        private boolean executable;
        @Json(name = "lamports")
        private long lamports;
        @Json(name = "owner")
        private String owner;
        @Json(name = "rentEpoch")
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
        @Json(name = "parsed")
        private Parsed parsed;

        public Parsed getParsed() {
            return parsed;
        }
    }

    public static class Parsed {
        @Json(name = "info")
        private Info info;

        public Info getInfo() {
            return info;
        }
    }

    public static class Info {
        @Json(name = "isNative")
        boolean isNative;
        @Json(name = "mint")
        String mint;
        @Json(name = "owner")
        String owner;
        @Json(name = "state")
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

    @Json(name = "value")
    private Value value;

    public Value getValue() {
        return value;
    }
}
