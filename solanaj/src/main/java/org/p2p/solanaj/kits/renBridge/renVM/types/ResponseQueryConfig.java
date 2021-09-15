package org.p2p.solanaj.kits.renBridge.renVM.types;

import java.util.List;

import com.squareup.moshi.Json;

public class ResponseQueryConfig {
    public static class Confirmations {
        @Json(name = "Bitcoin")
        public String bitcoin;
        @Json(name = "Ethereum")
        public String ethereum;

    }

    public static class MaxConfirmations {
        @Json(name = "Bitcoin")
        public String bitcoin;
        @Json(name = "Ethereum")
        public String ethereum;

    }

    public static class Registries {
        @Json(name = "Ethereum")
        public String ethereum;

    }

    @Json(name = "confirmations")
    public Confirmations confirmations;
    @Json(name = "maxConfirmations")
    public MaxConfirmations maxConfirmations;
    @Json(name = "network")
    public String network;
    @Json(name = "registries")
    public Registries registries;
    @Json(name = "whitelist")
    public List<String> whitelist = null;

}
