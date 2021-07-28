package org.p2p.solanaj.model.types;

import com.squareup.moshi.Json;

public class RpcResultTypes {

    public static class ValueLong extends RpcResultObject {
        @Json(name = "value")
        private long value;
    
        public long getValue() {
            return value;
        }
    }

}
