package org.p2p.solanaj.rpc;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RpcClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int TIMEOUT_INTERVAL = 90;

    private String endpoint;
    private final OkHttpClient httpClient;
    private final RpcApi rpcApi;

    public RpcClient(Environment endpoint, OkHttpClient client) {
        this(endpoint.getEndpoint(), client);
    }

    public RpcClient(String endpoint, OkHttpClient client) {
        this.endpoint = endpoint;
        rpcApi = new RpcApi(this);
        httpClient = client;
    }

    public void updateEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public <T> T call(String method, List<Object> params, Class<T> clazz) throws RpcException {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);
        JsonAdapter<RpcResponse<T>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, clazz));

        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(JSON, rpcRequestJsonAdapter.toJson(rpcRequest))).build();

        try {
            Response response = httpClient.newCall(request).execute();
            RpcResponse<T> rpcResult = resultAdapter.fromJson(response.body().string());

            if (rpcResult.getError() != null) {
                throw new RpcException(rpcResult.getError().getMessage());
            }

            return (T) rpcResult.getResult();
        } catch (IOException e) {
            throw new RpcException(e.getMessage());
        }
    }

    public RpcApi getApi() {
        return rpcApi;
    }
}
