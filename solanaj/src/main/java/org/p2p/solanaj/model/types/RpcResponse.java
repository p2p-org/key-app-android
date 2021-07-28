package org.p2p.solanaj.model.types;

import com.squareup.moshi.Json;

import java.util.List;

public class RpcResponse<T> {

    public static class Error {
        @Json(name = "code")
        private int code;

        @Json(name = "data")
        private Data data;

        @Json(name = "message")
        private String message;

        public int getCode(){
            return code;
        }

        public Data getData(){
            return data;
        }

        public String getMessage(){
            return message;
        }

    }

    public static class Data{

        @Json(name = "logs")
        private List<String> logs;

        public List<String> getLogs(){
            return logs;
        }
    }

    @Json(name = "jsonrpc")
    private String jsonrpc;
    @Json(name = "result")
    private T result;
    @Json(name = "error")
    private Error error;
    @Json(name = "id")
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