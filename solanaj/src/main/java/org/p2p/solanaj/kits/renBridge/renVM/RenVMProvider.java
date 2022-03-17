package org.p2p.solanaj.kits.renBridge.renVM;

import android.util.Log;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.kits.renBridge.NetworkConfig;
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryBlockState;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryConfig;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseSubmitTxMint;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.Hash;
import org.p2p.solanaj.utils.Utils;
import org.p2p.solanaj.utils.crypto.Base64UrlUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

public class RenVMProvider {
    private RpcClient client;
    private HashMap<String, Object> emptyParams = new HashMap<String, Object>();

    public RenVMProvider(NetworkConfig networkConfig) {
        this.client = new RpcClient(networkConfig.getLightNode());
    }

    public ResponseQueryTxMint queryMint(String txHash) throws RpcException {
        HashMap params = new HashMap();
        params.put("txHash", txHash);
        return client.callMap("ren_queryTx", params, ResponseQueryTxMint.class);
    }

    public ResponseQueryBlockState queryBlockState() throws RpcException {
        return client.callMap("ren_queryBlockState", emptyParams, ResponseQueryBlockState.class);
    }

    public ResponseQueryConfig queryConfig() throws RpcException {
        return client.callMap("ren_queryConfig", emptyParams, ResponseQueryConfig.class);
    }

    public ResponseSubmitTxMint submitTx(String hash, ParamsSubmitMint.MintTransactionInput mintTx, String selector)
            throws RpcException {
        ParamsSubmitMint params = new ParamsSubmitMint(hash, mintTx, selector);
        HashMap map = new HashMap();
        map.put("tx", params);
        return client.callMap("ren_submitTx", map, ResponseSubmitTxMint.class);
    }

    public byte[] selectPublicKey() throws RpcException {
        String pubKey = queryBlockState().getPubKey();
        return Base64UrlUtils.fromURLBase64(pubKey);
    }

    public String submitMint(byte[] gHash, byte[] gPubKey, byte[] nHash, byte[] nonce, String amount, byte[] pHash,
                             String to, String txIndex, byte[] txid) throws RpcException {
        String selector = "BTC/toSolana";
        ParamsSubmitMint.MintTransactionInput mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid);
        String hash = Utils.toURLBase64(hashTransactionMint(mintTx, selector));

        submitTx(hash, mintTx, selector);

        return hash;
    }

    public String submitBurn(byte[] gHash, byte[] gPubKey, byte[] nHash, byte[] nonce, String amount, byte[] pHash,
                             String to, String txIndex, byte[] txid) throws RpcException {
        String selector = "BTC/fromSolana";
        ParamsSubmitMint.MintTransactionInput mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid);
        String hash = Utils.toURLBase64(hashTransactionMint(mintTx, selector));

        submitTx(hash, mintTx, selector);

        return hash;
    }

    public String mintTxHash(byte[] gHash, byte[] gPubKey, byte[] nHash, byte[] nonce, String amount, byte[] pHash,
                             String to, String txIndex, byte[] txid) {
        ParamsSubmitMint.MintTransactionInput mintTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid);
        return Utils.toURLBase64(hashTransactionMint(mintTx, "BTC/toSolana"));
    }

    public String burnTxHash(byte[] gHash, byte[] gPubKey, byte[] nHash, byte[] nonce, String amount, byte[] pHash,
                             String to, String txIndex, byte[] txid) {
        ParamsSubmitMint.MintTransactionInput burnTx = buildTransaction(gHash, gPubKey, nHash, nonce, amount, pHash, to, txIndex, txid);
        return Utils.toURLBase64(hashTransactionMint(burnTx, "BTC/fromSolana"));
    }

    public BigInteger estimateTransactionFee() throws RpcException {
        ResponseQueryBlockState queryBlockState = queryBlockState();
        return new BigInteger(queryBlockState.state.v.btc.gasLimit)
                .multiply(new BigInteger(queryBlockState.state.v.btc.gasCap));
    }

    public static ParamsSubmitMint.MintTransactionInput buildTransaction(byte[] gHash, byte[] gPubKey, byte[] nHash, byte[] nonce,
                                                                         String amount, byte[] pHash, String to, String txIndex, byte[] txid) {
        ParamsSubmitMint.MintTransactionInput mintTx = new ParamsSubmitMint.MintTransactionInput();
        mintTx.txid = Utils.toURLBase64(txid);
        mintTx.txindex = txIndex;
        mintTx.ghash = Utils.toURLBase64(gHash);
        mintTx.gpubkey = gPubKey.length == 0 ? "" : Utils.toURLBase64(gPubKey);
        mintTx.nhash = Utils.toURLBase64(nHash);
        mintTx.nonce = Utils.toURLBase64(nonce);
        mintTx.phash = Utils.toURLBase64(pHash);
        mintTx.to = to;
        mintTx.amount = amount;
        return mintTx;
    }

    // txHash
    public static byte[] hashTransactionMint(ParamsSubmitMint.MintTransactionInput mintTx, String selector) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String version = "1";
        try {
            // todo: it was out.writeBytes() workaround
            out.write(marshalString(version));
            out.write(marshalString(selector));

            // marshalledType MintTransactionInput
            out.write(Base58.decode(
                    "aHQBEVgedhqiYDUtzYKdu1Qg1fc781PEV4D1gLsuzfpHNwH8yK2A2BuZK4uZoMC6pp8o7GWQxmsp52gsDrfbipkyeQZnXigCmscJY4aJDxF9tT8DQP3XRa1cBzQL8S8PTzi9nPnBkAxBhtNv6q1"));

            out.write(marshalBytes(Utils.fromURLBase64(mintTx.txid)));
            out.write(ByteUtils.uint32ToByteArrayBE(Long.valueOf(mintTx.txindex)));
            out.write(Utils.amountToUint256ByteArrayBE(mintTx.amount));
            out.write(new byte[]{0, 0, 0, 0});
            out.write(Utils.fromURLBase64(mintTx.phash));
            out.write(marshalString(mintTx.to));
            out.write(Utils.fromURLBase64(mintTx.nonce));
            out.write(Utils.fromURLBase64(mintTx.nhash));
            out.write(marshalBytes(Utils.fromURLBase64(mintTx.gpubkey)));
            out.write(Utils.fromURLBase64(mintTx.ghash));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Hash.sha256(out.toByteArray());
    }

    static byte[] marshalString(String src) {
        return marshalBytes(src.getBytes());
    }

    static byte[] marshalBytes(byte[] in) {
        byte[] out = new byte[ByteUtils.UINT_32_LENGTH + in.length];
        System.arraycopy(ByteUtils.uint32ToByteArrayBE(in.length), 0, out, 0, ByteUtils.UINT_32_LENGTH);
        System.arraycopy(in, 0, out, ByteUtils.UINT_32_LENGTH, in.length);
        return out;
    }

}
