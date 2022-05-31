package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ResponseQueryConfig {
    public static class Confirmations {
        @SerializedName("Bitcoin")
        public String bitcoin;
        @SerializedName("Ethereum")
        public String ethereum;

    }

    public static class MaxConfirmations {
        @SerializedName("Bitcoin")
        public String bitcoin;
        @SerializedName("Ethereum")
        public String ethereum;

    }

    public static class Registries {
        @SerializedName("Ethereum")
        public String ethereum;

    }

    @SerializedName("confirmations")
    public Confirmations confirmations;
    @SerializedName("maxConfirmations")
    public MaxConfirmations maxConfirmations;
    @SerializedName("network")
    public String network;
    @SerializedName("registries")
    public Registries registries;
    @SerializedName("whitelist")
    public List<String> whitelist = null;

}
