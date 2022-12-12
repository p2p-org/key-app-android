package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ParamsSubmitMint {
    public static class In {
        @SerializedName("t")
        public TypeIn typeIn = new TypeIn();

        @SerializedName("v")
        public MintTransactionInput mintTransactionInput;

        public In(MintTransactionInput mintTransactionInput) {
            this.mintTransactionInput = mintTransactionInput;
        }

    }

    public static class MintTransactionInput {
        @SerializedName("txid")
        public String txid;
        @SerializedName("txindex")
        public String txindex;
        @SerializedName("ghash")
        public String ghash = "";
        @SerializedName("gpubkey")
        public String gpubkey;
        @SerializedName("nhash")
        public String nhash;
        @SerializedName("nonce")
        public String nonce;
        @SerializedName("payload")
        public String payload = "";
        @SerializedName("phash")
        public String phash;
        @SerializedName("to")
        public String to;
        @SerializedName("amount")
        public String amount;

    }

    public static class TypeIn {
        @SerializedName("struct")
        public List<Object> struct = Arrays.asList(Collections.singletonMap("txid", "bytes"), Collections.singletonMap("txindex", "u32"),
                Collections.singletonMap("amount", "u256"), Collections.singletonMap("payload", "bytes"), Collections.singletonMap("phash", "bytes32"),
                Collections.singletonMap("to", "string"), Collections.singletonMap("nonce", "bytes32"), Collections.singletonMap("nhash", "bytes32"),
                Collections.singletonMap("gpubkey", "bytes"), Collections.singletonMap("ghash", "bytes32"));

    }

    @SerializedName("hash")
    public String hash;
    @SerializedName("selector")
    public String selector;
    @SerializedName("version")
    public String version = "1";
    @SerializedName("in")
    public In in;

    public ParamsSubmitMint(String hash, MintTransactionInput mintTransactionInput, String selector) {
        this.hash = hash;
        this.in = new In(mintTransactionInput);
        this.selector = selector;
    }

}