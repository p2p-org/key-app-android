package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;
import com.squareup.moshi.Json;

public class ResponseQueryBlockState {
    public static class Btc {
        @Json(name = "fees")
        public Fees fees;
        @Json(name = "gasCap")
        public String gasCap;
        @Json(name = "gasLimit")
        public String gasLimit;
        @Json(name = "gasPrice")
        public String gasPrice;
        @Json(name = "latestHeight")
        public String latestHeight;
        @Json(name = "minimumAmount")
        public String minimumAmount;
        @Json(name = "shards")
        public List<Shard> shards = null;

    }

    public static class Chain {
        @Json(name = "burnFee")
        public String burnFee;
        @Json(name = "chain")
        public String chain;
        @Json(name = "mintFee")
        public String mintFee;

    }

    public static class Fees {
        @Json(name = "chains")
        public List<Chain> chains = null;

    }

    public static class Outpoint {
        @Json(name = "hash")
        public String hash;
        @Json(name = "index")
        public String index;

    }

    public static class Shard {
        @Json(name = "pubKey")
        public String pubKey;
        @Json(name = "shard")
        public String shard;
        @Json(name = "state")
        public ShardState state;

    }

    public static class ShardState {
        @Json(name = "outpoint")
        public Outpoint outpoint;
        @Json(name = "pubKeyScript")
        public String pubKeyScript;
        @Json(name = "value")
        public String value;

    }

    public static class Values {
        @Json(name = "BTC")
        public Btc btc;

    }

    public static class State {
        @Json(name = "v")
        public Values v;
    }

    @Json(name = "state")
    public State state;

    public List<Shard> getShardList() {
        return state.v.btc.shards;
    }

    public String getPubKey() {
        return getShardList().get(0).pubKey;
    }

}