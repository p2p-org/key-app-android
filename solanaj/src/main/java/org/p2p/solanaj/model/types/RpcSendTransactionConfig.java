package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RpcSendTransactionConfig {

    public static enum Encoding {
        base64("base64");

        private String enc;

        Encoding(String enc) {
            this.enc = enc;
        }

        public String getEncoding() {
            return enc;
        }

    }

    @SerializedName("encoding")
    private Encoding encoding = Encoding.base64;

}
