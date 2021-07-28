package org.p2p.solanaj.model.types;

import com.squareup.moshi.Json;

public class RpcQrAccountInfoConfig {

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

    @Json(name = "encoding")
    private Encoding encoding = Encoding.jsonParsed;

}
