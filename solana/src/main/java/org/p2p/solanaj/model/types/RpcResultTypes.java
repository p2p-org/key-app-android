package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;

public class RpcResultTypes {

    public static class ValueLong extends RpcResultObject {
        @SerializedName("value")
        private long value;
    
        public long getValue() {
            return value;
        }
    }

}
