package org.p2p.solanaj.rpc;

import android.util.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.p2p.solanaj.BuildConfig;
import org.p2p.solanaj.model.types.RpcRequest;
import org.p2p.solanaj.model.types.RpcRequest2;
import org.p2p.solanaj.model.types.RpcResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RpcClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String endpoint;
    private OkHttpClient httpClient = new OkHttpClient();
    private RpcApi rpcApi;

    public RpcClient(Environment endpoint) {
        this(endpoint.getEndpoint());
    }

    public RpcClient(String endpoint) {
        this.endpoint = endpoint;
        rpcApi = new RpcApi(this);
    }

    public <T> T call(String method, List<Object> params, Class<T> clazz) throws RpcException {
        List<Object> parameters;

        if (params == null || params.isEmpty()) {
            parameters = new ArrayList();
        } else {
            parameters = params;
        }
        RpcRequest rpcRequest = new RpcRequest(method, parameters);

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class).lenient();
        JsonAdapter<RpcResponse<T>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Type.class.cast(clazz)));

        String requestData = rpcRequestJsonAdapter.toJson(rpcRequest);
        if (BuildConfig.DEBUG) Log.d("RpcClient", "Json request: " + requestData);

        Request request = new Request.Builder().url(endpoint)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, requestData)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            String bodyString = response.body().string();
            if (response.isSuccessful()) {
                System.out.println("RpcClient: " + bodyString);
                RpcResponse<T> rpcResult = resultAdapter.lenient().fromJson(bodyString);

                if (rpcResult.getError() != null) {
                    throw new RpcException(rpcResult.getError().getMessage());
                }

                return (T) rpcResult.getResult();
            } else {
                System.out.println("RpcClientError: " + bodyString);
                RpcResponse<T> rpcResult = resultAdapter.lenient().fromJson(bodyString);
                throw new RpcException(rpcResult.getError().getMessage());
            }
        } catch (IOException e) {
            throw new RpcException(e.getMessage());
        }
    }

    public <T> T callMap(String method, Map<String, Object> params, Class<T> clazz) throws RpcException {
        RpcRequest2 rpcRequest = new RpcRequest2(method, params);

        JsonAdapter<RpcRequest2> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest2.class);
        JsonAdapter<RpcResponse<T>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Type.class.cast(clazz)));

        String jsonRequest = rpcRequestJsonAdapter.toJson(rpcRequest);
        if (BuildConfig.DEBUG) Log.d("RpcClient", "Json request: " + jsonRequest);
        Request request = new Request.Builder().url(endpoint)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, jsonRequest)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            String bodyString = response.body().string();
            if (BuildConfig.DEBUG) Log.d("RpcClient", "Json response: " + bodyString);
            RpcResponse<T> rpcResult = resultAdapter.fromJson(bodyString);

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

    public String getEndpoint() {
        return endpoint;
    }

}