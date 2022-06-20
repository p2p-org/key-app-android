package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResponseQueryTxMint {

    public static class In {
        @SerializedName("v")
        public ValueIn valueIn;

    }

    public static class Tx {
        @SerializedName("hash")
        public String hash;
        @SerializedName("version")
        public String version;
        @SerializedName("selector")
        public String selector;
        @SerializedName("in")
        public In in;
        @SerializedName("out")
        public Out out;

    }

    public static class ValueIn {
        @SerializedName("amount")
        public String amount;
        @SerializedName("ghash")
        public String ghash;
        @SerializedName("gpubkey")
        public String gpubkey;
        @SerializedName("nhash")
        public String nhash;
        @SerializedName("nonce")
        public String nonce;
        @SerializedName("payload")
        public String payload;
        @SerializedName("phash")
        public String phash;
        @SerializedName("to")
        public String to;
        @SerializedName("txid")
        public String txid;
        @SerializedName("txindex")
        public long txindex;

    }

    public static class Out {
        @SerializedName("t")
        public TypeOut typeOut;
        @SerializedName("v")
        public ValueOut valueOut;

    }

    public static class TypeOut {
        @SerializedName("struct")
        public List<OutStructType> struct = null;

    }

    public static class OutStructType {
        @SerializedName("hash")
        public String hash;
        @SerializedName("amount")
        public String amount;
        @SerializedName("sighash")
        public String sighash;
        @SerializedName("sig")
        public String sig;
        @SerializedName("txid")
        public String txid;
        @SerializedName("txindex")
        public String txindex;

    }

    public static class ValueOut {
        @SerializedName("amount")
        public String amount;
        @SerializedName("hash")
        public String hash;
        @SerializedName("sig")
        public String sig;
        @SerializedName("sighash")
        public String sighash;
        @SerializedName("txid")
        public String txid;
        @SerializedName("txindex")
        public String txindex;
    }

    @SerializedName("tx")
    public Tx tx;
    @SerializedName("txStatus")
    public String txStatus;

    public ValueOut getValueOut() {
        return tx.out.valueOut;
    }

    public ValueIn getValueIn() {
        return tx.in.valueIn;
    }

}