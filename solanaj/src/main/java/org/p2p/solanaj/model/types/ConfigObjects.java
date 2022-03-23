package org.p2p.solanaj.model.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import org.p2p.solanaj.model.types.RpcSendTransactionConfig.Encoding;

public class ConfigObjects {

    public static class ConfirmedSignFAddr2 {
        @SerializedName("limit")
        private long limit;
        @SerializedName("before")
        private String before;
        @SerializedName("until")
        private String until;

        public ConfirmedSignFAddr2(String before, int limit) {
            this.limit = limit;
            this.before = before;

        }
        public ConfirmedSignFAddr2(int limit) {
            this.limit = limit;
            this.before = before;
        }
    }

    public static class Memcmp {
        @SerializedName("offset")
        private long offset;
        @SerializedName("bytes")
        private String bytes;

        public Memcmp() {

        }

        public Memcmp(long offset, String bytes) {
            this.offset = offset;
            this.bytes = bytes;
        }

    }

    public static class DataSize {
        public static final int SPAN = 165;
        @SerializedName("dataSize")
        private int dataSize;

        public DataSize(int dataSize) {
            this.dataSize = dataSize;
        }
    }

    public static class Filter {
        @SerializedName("memcmp")
        private Memcmp memcmp;

        public Filter() {
        }

        public Filter(Memcmp memcmp) {
            this.memcmp = memcmp;
        }

    }

    public static class ProgramAccountConfig {
        @SerializedName("encoding")
        private Encoding encoding = null;
        @SerializedName("filters")
        private List<Object> filters = null;

        public ProgramAccountConfig() {
        }

        public ProgramAccountConfig(List<Object> filters) {
            this.filters = filters;
        }

        public ProgramAccountConfig(Encoding encoding) {
            this.encoding = encoding;
        }

    }
}
