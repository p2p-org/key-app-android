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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RpcClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "RpcClient";

    private final String endpoint;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final RpcApi rpcApi;

    public RpcClient(Environment endpoint) {
        this(endpoint.getEndpoint());
    }

    public RpcClient(String endpoint) {
        this.endpoint = endpoint;
        rpcApi = new RpcApi(this);
    }

    @SuppressWarnings("ConstantConditions")
    public <T> T call(String method, List<Object> params, Class<T> clazz) throws RpcException {
        List<Object> parameters;

        if (params == null || params.isEmpty()) {
            parameters = new ArrayList<>();
        } else {
            parameters = params;
        }
        RpcRequest rpcRequest = new RpcRequest(method, parameters);

        Moshi moshi = new Moshi.Builder().build();

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class).lenient();

        ParameterizedType bodyType = Types.newParameterizedType(RpcResponse.class, (Type) clazz);
        JsonAdapter<RpcResponse<T>> resultAdapter = moshi.adapter(bodyType);

        String requestData = rpcRequestJsonAdapter.toJson(rpcRequest);
        if (BuildConfig.DEBUG) Log.d(TAG, "Json request: " + requestData);

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, requestData)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            String bodyString = response.body().string();
            if (response.isSuccessful()) {
                if (BuildConfig.DEBUG) Log.d(TAG, bodyString);
                RpcResponse<T> rpcResult = resultAdapter.lenient().fromJson(bodyString);

                if (rpcResult.getError() != null) {
                    throw new RpcException(rpcResult.getError().getMessage());
                }

                return rpcResult.getResult();
            } else {
                if (BuildConfig.DEBUG) Log.d("RpcClientError:", bodyString);
                RpcResponse<T> rpcResult = resultAdapter.lenient().fromJson(bodyString);
                throw new RpcException(rpcResult.getError().getMessage());
            }
        } catch (IOException e) {
            throw new RpcException(e.getMessage());
        } catch (NullPointerException npe){
            Log.e(TAG, npe.getMessage(), npe);
            throw npe;
        }
    }

    public <T> T callMap(String method, Map<String, Object> params, Class<T> clazz) throws RpcException {
        RpcRequest2 rpcRequest = new RpcRequest2(method, params);

        Moshi moshi = new Moshi.Builder().build();

        JsonAdapter<RpcRequest2> rpcRequestJsonAdapter = moshi.adapter(RpcRequest2.class);
        ParameterizedType bodyType = Types.newParameterizedType(RpcResponse.class, (Type) clazz);
        JsonAdapter<RpcResponse<T>> resultAdapter = moshi.adapter(bodyType);

        String jsonRequest = rpcRequestJsonAdapter.toJson(rpcRequest);
        if (BuildConfig.DEBUG) Log.d(TAG, "Json request: " + jsonRequest);
        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, jsonRequest)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            String bodyString = response.body().string();
            if (BuildConfig.DEBUG) Log.d(TAG, "Json response: " + bodyString);
            RpcResponse<T> rpcResult = resultAdapter.fromJson(bodyString);

            if (rpcResult.getError() != null) {
                throw new RpcException(rpcResult.getError().getMessage());
            }

            return rpcResult.getResult();
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