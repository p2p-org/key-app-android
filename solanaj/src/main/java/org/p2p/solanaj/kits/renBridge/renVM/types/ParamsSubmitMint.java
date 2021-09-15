package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.squareup.moshi.Json;

public class ParamsSubmitMint {
    public static class In {
        @Json(name = "t")
        public TypeIn typeIn = new TypeIn();

        @Json(name = "v")
        public MintTransactionInput mintTransactionInput;

        public In(MintTransactionInput mintTransactionInput) {
            this.mintTransactionInput = mintTransactionInput;
        }

    }

    public static class MintTransactionInput {
        @Json(name = "txid")
        public String txid;
        @Json(name = "txindex")
        public String txindex;
        @Json(name = "ghash")
        public String ghash = "";
        @Json(name = "gpubkey")
        public String gpubkey;
        @Json(name = "nhash")
        public String nhash;
        @Json(name = "nonce")
        public String nonce;
        @Json(name = "payload")
        public String payload = "";
        @Json(name = "phash")
        public String phash;
        @Json(name = "to")
        public String to;
        @Json(name = "amount")
        public String amount;

    }

    public static class TypeIn {
        @Json(name = "struct")
        public List<Object> struct = Arrays.asList(Map.of("txid", "bytes"), Map.of("txindex", "u32"),
                Map.of("amount", "u256"), Map.of("payload", "bytes"), Map.of("phash", "bytes32"),
                Map.of("to", "string"), Map.of("nonce", "bytes32"), Map.of("nhash", "bytes32"),
                Map.of("gpubkey", "bytes"), Map.of("ghash", "bytes32"));

    }

    @Json(name = "hash")
    public String hash;
    @Json(name = "selector")
    public String selector;
    @Json(name = "version")
    public String version = "1";
    @Json(name = "in")
    public In in;

    public ParamsSubmitMint(String hash, MintTransactionInput mintTransactionInput, String selector) {
        this.hash = hash;
        this.in = new In(mintTransactionInput);
        this.selector = selector;
    }

}