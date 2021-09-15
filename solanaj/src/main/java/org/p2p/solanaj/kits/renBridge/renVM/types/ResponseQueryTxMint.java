package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;
import com.squareup.moshi.Json;

public class ResponseQueryTxMint {

    public static class In {
        @Json(name = "v")
        public ValueIn valueIn;

    }

    public static class Tx {
        @Json(name = "hash")
        public String hash;
        @Json(name = "version")
        public String version;
        @Json(name = "selector")
        public String selector;
        @Json(name = "in")
        public In in;
        @Json(name = "out")
        public Out out;

    }

    public static class ValueIn {
        @Json(name = "amount")
        public String amount;
        @Json(name = "ghash")
        public String ghash;
        @Json(name = "gpubkey")
        public String gpubkey;
        @Json(name = "nhash")
        public String nhash;
        @Json(name = "nonce")
        public String nonce;
        @Json(name = "payload")
        public String payload;
        @Json(name = "phash")
        public String phash;
        @Json(name = "to")
        public String to;
        @Json(name = "txid")
        public String txid;
        @Json(name = "txindex")
        public long txindex;

    }

    public static class Out {
        @Json(name = "t")
        public TypeOut typeOut;
        @Json(name = "v")
        public ValueOut valueOut;

    }

    public static class TypeOut {
        @Json(name = "struct")
        public List<OutStructType> struct = null;

    }

    public static class OutStructType {
        @Json(name = "hash")
        public String hash;
        @Json(name = "amount")
        public String amount;
        @Json(name = "sighash")
        public String sighash;
        @Json(name = "sig")
        public String sig;
        @Json(name = "txid")
        public String txid;
        @Json(name = "txindex")
        public String txindex;

    }

    public static class ValueOut {
        @Json(name = "amount")
        public String amount;
        @Json(name = "hash")
        public String hash;
        @Json(name = "sig")
        public String sig;
        @Json(name = "sighash")
        public String sighash;
        @Json(name = "txid")
        public String txid;
        @Json(name = "txindex")
        public String txindex;
    }

    @Json(name = "tx")
    public Tx tx;
    @Json(name = "txStatus")
    public String txStatus;

    public ValueOut getValueOut() {
        return tx.out.valueOut;
    }

    public ValueIn getValueIn() {
        return tx.in.valueIn;
    }

}