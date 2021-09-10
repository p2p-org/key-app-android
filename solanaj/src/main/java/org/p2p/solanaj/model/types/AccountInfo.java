package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import androidx.annotation.Nullable;

public class AccountInfo extends RpcResultObject {

    public static class Value {
        @SerializedName("data")
        private List<String> data = null;
        @SerializedName("executable")
        private boolean executable;
        @SerializedName("lamports")
        private long lamports;
        @SerializedName("owner")
        private String owner;
        @SerializedName("rentEpoch")
        private long rentEpoch;

        @Nullable
        public List<String> getData() {
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

    @SerializedName("value")
    private Value value;

    public Value getValue() {
        return value;
    }

}
