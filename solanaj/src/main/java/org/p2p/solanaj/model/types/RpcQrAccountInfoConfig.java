package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RpcQrAccountInfoConfig {

    public static enum Encoding {
        @SerializedName("jsonParsed")
        jsonParsed("jsonParsed");

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
    private Encoding encoding = Encoding.jsonParsed;

}
