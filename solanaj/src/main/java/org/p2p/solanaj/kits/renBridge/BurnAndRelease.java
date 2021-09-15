package org.p2p.solanaj.kits.renBridge;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.Hash;
import org.p2p.solanaj.utils.Utils;
import org.p2p.solanaj.utils.crypto.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class BurnAndRelease {
    private RenVMProvider renVMProvider;
    private SolanaChain solanaChain;
    private LockAndMint.State state = new LockAndMint.State();
    private byte[] nonceBuffer;
    private String recepient;

    public BurnAndRelease(NetworkConfig networkConfig) throws Exception {
        this.renVMProvider = new RenVMProvider(networkConfig);
        this.solanaChain = new SolanaChain(new RpcClient(networkConfig.getEndpoint()), networkConfig);
    }

    public SolanaChain.BurnDetails submitBurnTransaction(PublicKey account, String amount, String recepient, Account signer)
            throws Exception {
        this.recepient = recepient;
        return solanaChain.submitBurn(account, amount, recepient, signer);
    }

    public LockAndMint.State getBurnState(SolanaChain.BurnDetails burnDetails, String amount) throws Exception {
        byte[] txid = Base58.decode(burnDetails.confirmedSignature);
        nonceBuffer = getNonceBuffer(burnDetails.nonce);
        byte[] nHash = Hash.generateNHash(nonceBuffer, txid, "0");
        byte[] pHash = Hash.generatePHash();
        byte[] sHash = Hash.generateSHash("BTC/toBitcoin");
        byte[] gHash = Hash.generateGHash(Hex.INSTANCE.encode(Utils.addressToBytes(burnDetails.recepient)), Hex.INSTANCE.encode(sHash),
                nonceBuffer);

        String txHash = renVMProvider.burnTxHash(gHash, new byte[]{}, nHash, nonceBuffer, amount, pHash,
                burnDetails.recepient, "0", txid);

        state.txIndex = "0";
        state.amount = amount;
        state.nHash = nHash;
        state.txid = txid;
        state.pHash = pHash;
        state.gHash = gHash;
        state.txHash = txHash;
        state.gPubKey = new byte[]{};

        return state;
    }

    public String release() throws RpcException {
        String txHash = renVMProvider.submitBurn(state.gHash, state.gPubKey, state.nHash, nonceBuffer, state.amount,
                state.pHash, recepient, state.txIndex, state.txid);
        return txHash;
    }

    public static byte[] getNonceBuffer(BigInteger nonce) {
        byte[] amountBytes = nonce.toByteArray();
        ByteBuffer amountBuffer = ByteBuffer.allocate(32);
        amountBuffer.position(32 - amountBytes.length);
        amountBuffer.put(amountBytes);
        return amountBuffer.array();
    }

}
