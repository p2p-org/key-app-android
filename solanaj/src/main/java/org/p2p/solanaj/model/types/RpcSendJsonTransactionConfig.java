package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RpcSendJsonTransactionConfig {

    public static enum Encoding {
        jsonParsed("jsonParsed");

        private String enc;

        Encoding(String enc) {
            this.enc = enc;
        }

        public String getEncoding() {
            return enc;
        }

    }

    @SerializedName("encoding")
    private Encoding encoding = Encoding.jsonParsed;

}
