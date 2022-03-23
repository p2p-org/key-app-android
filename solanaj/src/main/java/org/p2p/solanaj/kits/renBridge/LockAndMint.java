package org.p2p.solanaj.kits.renBridge;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint;
import org.p2p.solanaj.kits.renBridge.script.Script;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.utils.Hash;
import org.p2p.solanaj.utils.Utils;
import org.p2p.solanaj.utils.crypto.Hex;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class LockAndMint {
    private NetworkConfig networkConfig;
    private RenVMProvider renVMProvider;
    private Session session;
    private SolanaChain solanaChain;
    private State state = new State();

    public static LockAndMint buildSession(NetworkConfig networkConfig, PublicKey destinationAddress) throws Exception {
        return new LockAndMint(networkConfig, new RenVMProvider(networkConfig),
                new SolanaChain(new RpcClient(networkConfig.getEndpoint()), networkConfig),
                new Session(destinationAddress));
    }

    public static LockAndMint getSession(NetworkConfig networkConfig, Session session) throws Exception {
        return new LockAndMint(networkConfig, new RenVMProvider(networkConfig),
                new SolanaChain(new RpcClient(networkConfig.getEndpoint()), networkConfig),
                session);
    }

    public LockAndMint(NetworkConfig networkConfig, RenVMProvider renVMProvider, SolanaChain solanaChain,
                       Session session) {
        this.networkConfig = networkConfig;
        this.renVMProvider = renVMProvider;
        this.session = session;
        this.solanaChain = solanaChain;
    }

    public String generateGatewayAddress() throws Exception {
        PublicKey sendTo = solanaChain.getAssociatedTokenAddress(session.destinationAddress);
        state.sendTo = sendTo;
        String sendToHex = Hex.INSTANCE.encode(sendTo.asByteArray());
        String tokenGatewayContractHex = Hex.INSTANCE.encode(Hash.generateSHash());
        byte[] gHash = Hash.generateGHash(sendToHex, tokenGatewayContractHex, Hex.INSTANCE.decode(session.nonce));
        state.gHash = gHash;
        byte[] gPubKey = renVMProvider.selectPublicKey();
        state.gPubKey = gPubKey;

        byte[] gatewayAddress = Script.createAddressByteArray(Hash.hash160(gPubKey), gHash,
                new byte[]{(byte) networkConfig.getP2shPrefix()});

        session.gatewayAddress = Base58.encode(gatewayAddress);
        return session.gatewayAddress;
    }

    public State getDepositState(String transactionHash, String txIndex, String amount) throws Exception {
        byte[] nonce = Hex.INSTANCE.decode(session.nonce);
        byte[] txid = Hex.INSTANCE.decode(Utils.reverseHex(transactionHash));
        byte[] nHash = Hash.generateNHash(nonce, txid, txIndex);
        byte[] pHash = Hash.generatePHash();

        String txHash = renVMProvider.mintTxHash(state.gHash, state.gPubKey, nHash, nonce, amount, pHash,
                state.sendTo.toBase58(), txIndex, txid);

        state.txIndex = txIndex;
        state.amount = amount;
        state.nHash = nHash;
        state.txid = txid;
        state.pHash = pHash;
        state.txHash = txHash;

        return state;
    }

    public String submitMintTransaction() throws RpcException {
        String txHash = renVMProvider.submitMint(state.gHash, state.gPubKey, state.nHash, Hex.INSTANCE.decode(session.nonce),
                state.amount, state.pHash, state.sendTo.toBase58(), state.txIndex, state.txid);
        return txHash;
    }

    public String mint(Account signer) throws Exception {
        ResponseQueryTxMint responseQueryMint = renVMProvider.queryMint(state.txHash);
        String signature = solanaChain.submitMint(session.destinationAddress, signer, responseQueryMint);
        return signature;
    }

    public ResponseQueryTxMint lockAndMint(String txHash) throws Exception {
        ResponseQueryTxMint responseQueryMint = renVMProvider.queryMint(txHash);
        return responseQueryMint;
    }

    public BigInteger estimateTransactionFee() throws Exception {
        BigInteger fee = renVMProvider.estimateTransactionFee();
        session.fee = fee;
        return fee;
    }

    public Session getSession() {
        return session;
    }

    public static class State {
        public byte[] gHash;
        public byte[] gPubKey;
        public PublicKey sendTo;
        public byte[] txid;
        public byte[] nHash;
        public byte[] pHash;
        public String txHash;
        public String txIndex;
        public String amount;

    }

    public static class Session {
        public PublicKey destinationAddress;
        public String nonce;
        public long createdAt;
        public long expiryTime;
        public String gatewayAddress;
        public BigInteger fee;

        public Session(PublicKey destinationAddress) {
            this.destinationAddress = destinationAddress;
            this.nonce = Utils.generateNonce();
            this.createdAt = System.currentTimeMillis();
            this.expiryTime = Utils.getSessionExpiry();
        }

        public Session(
                PublicKey destinationAddress,
                String nonce,
                long createdAt,
                long expiryTime,
                String gatewayAddress
        ) {
            this.destinationAddress = destinationAddress;
            this.nonce = nonce;
            this.createdAt = createdAt;
            this.expiryTime = expiryTime;
            this.gatewayAddress = gatewayAddress;
        }

        public Session(
                PublicKey destinationAddress,
                String nonce,
                long createdAt,
                long expiryTime,
                String gatewayAddress,
                BigInteger fee
        ) {
            this.destinationAddress = destinationAddress;
            this.nonce = nonce;
            this.createdAt = createdAt;
            this.expiryTime = expiryTime;
            this.gatewayAddress = gatewayAddress;
            this.fee = fee;
        }

        public Boolean isValid() {
            long dayInMillis = TimeUnit.DAYS.toMillis(1);
            long currentTime = System.currentTimeMillis();
            /* We should subtract one day from expiry time to make it valid  */
            return currentTime < (expiryTime - dayInMillis);
        }
    }

}
