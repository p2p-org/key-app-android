package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RpcSendTransactionConfig {

    public static enum Encoding {
        @SerializedName("base64")
        base64("base64");

        @SerializedName("enc")
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
