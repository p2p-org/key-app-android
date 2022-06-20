package org.p2p.solanaj.model.types;

import com.google.gson.annotations.SerializedName;


public class RpcResponse<T> {

    public static class Error {
        @SerializedName("code")
        private long code;
        @SerializedName("message")
        private String message;

        public long getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

    }

    @SerializedName("jsonrpc")
    private String jsonrpc;
    @SerializedName("result")
    private T result;
    @SerializedName("error")
    private Error error;
    @SerializedName("id")
    private String id;

    public Error getError() {
        return error;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getId() {
        return id;
    }

}
