package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResponseQueryBlockState {
    public static class Btc {
        @SerializedName("fees")
        public Fees fees;
        @SerializedName("gasCap")
        public String gasCap;
        @SerializedName("gasLimit")
        public String gasLimit;
        @SerializedName("gasPrice")
        public String gasPrice;
        @SerializedName("latestHeight")
        public String latestHeight;
        @SerializedName("minimumAmount")
        public String minimumAmount;
        @SerializedName("shards")
        public List<Shard> shards = null;

    }

    public static class Chain {
        @SerializedName("burnFee")
        public String burnFee;
        @SerializedName("chain")
        public String chain;
        @SerializedName("mintFee")
        public String mintFee;

    }

    public static class Fees {
        @SerializedName("chains")
        public List<Chain> chains = null;

    }

    public static class Outpoint {
        @SerializedName("hash")
        public String hash;
        @SerializedName("index")
        public String index;

    }

    public static class Shard {
        @SerializedName("pubKey")
        public String pubKey;
        @SerializedName("shard")
        public String shard;
        @SerializedName("state")
        public ShardState state;

    }

    public static class ShardState {
        @SerializedName("outpoint")
        public Outpoint outpoint;
        @SerializedName("pubKeyScript")
        public String pubKeyScript;
        @SerializedName("value")
        public String value;

    }

    public static class Values {
        @SerializedName("BTC")
        public Btc btc;
    }

    public static class State {
        @SerializedName("v")
        public Values v;
    }

    @SerializedName("state")
    public State state;

    public List<Shard> getShardList() {
        return state.v.btc.shards;
    }

    public String getPubKey() {
        return getShardList().get(0).pubKey;
    }

}