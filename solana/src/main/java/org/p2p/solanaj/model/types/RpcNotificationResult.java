package org.p2p.solanaj.model.types;


import com.google.gson.annotations.SerializedName;

import org.p2p.solanaj.model.types.RpcResultObject;

public class RpcNotificationResult {

    public static class Result extends RpcResultObject {
        @SerializedName("value")
        private Object value;

        public Object getValue() {
            return value;
        }
    }

    public static class Params {

        @SerializedName("result")
        private Result result;
        @SerializedName("subscription")
        private long subscription;

        public Result getResult() {
            return result;
        }

        public long getSubscription() {
            return subscription;
        }

    }

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("method")
    private String method;
    @SerializedName("params")
    private Params params;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public Params getParams() {
        return params;
    }

} 