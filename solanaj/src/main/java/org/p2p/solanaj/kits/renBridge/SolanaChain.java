package org.p2p.solanaj.kits.renBridge;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.AbstractData;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.kits.TokenTransaction;
import org.p2p.solanaj.kits.renBridge.renVM.types.ResponseQueryTxMint;
import org.p2p.solanaj.model.types.AccountInfo;
import org.p2p.solanaj.model.types.SignatureInformationResponse;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.Hash;
import org.p2p.solanaj.utils.Utils;
import org.p2p.solanaj.utils.crypto.Base64Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolanaChain {
    public GatewayRegistryData gatewayRegistryData;

    public static String GatewayRegistryStateKey = "GatewayRegistryState";
    public static String GatewayStateKey = "GatewayStateV0.1.4";

    private RpcClient client;

    public SolanaChain(RpcClient client, NetworkConfig networkConfig) throws Exception {
        this.client = client;

        PublicKey pubk = new PublicKey(networkConfig.getGatewayRegistry());
        PublicKey stateKey = PublicKey.Companion.findProgramAddress(
                Arrays.asList(GatewayRegistryStateKey.getBytes()),
                pubk
        )
                .getAddress();
        AccountInfo accountInfo = client.getApi().getAccountInfo(stateKey);
        String base64Data = accountInfo.getValue().getData().get(0);
        gatewayRegistryData = GatewayRegistryData.decode(Base64Utils.INSTANCE.decode(base64Data));
    }

    public PublicKey resolveTokenGatewayContract() {
        if (this.gatewayRegistryData == null) {
            throw new RuntimeException("chain not initialized");
        }

        String sHash = Base58.encode(Hash.generateSHash());
        int index = this.gatewayRegistryData.getSelectors().indexOf(sHash);

        return this.gatewayRegistryData.getGateways().get(index);
    }

    public PublicKey getSPLTokenPubkey() throws Exception {
        PublicKey program = resolveTokenGatewayContract();
        String sHash = Base58.encode(Hash.generateSHash());
        PublicKey mint = PublicKey.Companion.findProgramAddress(Arrays.asList(Base58.decode(sHash)), program).getAddress();
        return mint;
    }

    public PublicKey getAssociatedTokenAddress(PublicKey address) throws Exception {
        PublicKey mint = getSPLTokenPubkey();
        PublicKey destination = TokenTransaction.INSTANCE.getAssociatedTokenAddress(mint, address);
        return destination;
    }

    public String createAssociatedTokenAccount(PublicKey address, Account signer) throws Exception {
        PublicKey tokenMint = getSPLTokenPubkey();
        PublicKey associatedTokenAddress = getAssociatedTokenAddress(address);

        TransactionInstruction createAccountInstruction = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                tokenMint,
                associatedTokenAddress,
                address,
                signer.getPublicKey()
        );

        Transaction transaction = new Transaction();
        transaction.addInstruction(createAccountInstruction);

        return client.getApi().sendTransaction(transaction, signer);
    }

    public String submitMint(PublicKey address, Account signer, ResponseQueryTxMint responceQueryMint)
            throws Exception {
        byte[] pHash = Utils.fromURLBase64(responceQueryMint.getValueIn().phash);
        String amount = responceQueryMint.getValueOut().amount;
        byte[] nHash = Utils.fromURLBase64(responceQueryMint.getValueIn().nhash);
        byte[] sig = Utils.fixSignatureSimple(responceQueryMint.getValueOut().sig);

        PublicKey program = resolveTokenGatewayContract();
        PublicKey gatewayAccountId = PublicKey.Companion.findProgramAddress(Arrays.asList(GatewayStateKey.getBytes()), program)
                .getAddress();
        byte[] sHash = Hash.generateSHash();
        PublicKey tokenMint = getSPLTokenPubkey();
        PublicKey mintAuthority = PublicKey.Companion.findProgramAddress(Arrays.asList(tokenMint.asByteArray()), program)
                .getAddress();
        PublicKey recipientTokenAccount = getAssociatedTokenAddress(address);

        byte[] renVMMessage = buildRenVMMessage(pHash, amount, sHash, recipientTokenAccount.asByteArray(), nHash);
        PublicKey mintLogAccount = PublicKey.Companion.findProgramAddress(Arrays.asList(Hash.keccak256(renVMMessage)), program)
                .getAddress();

        TransactionInstruction mintInstruction = RenProgram.mintInstruction(signer.getPublicKey(), gatewayAccountId,
                tokenMint, recipientTokenAccount, mintLogAccount, mintAuthority, program);

        AccountInfo gatewayInfo = client.getApi().getAccountInfo(gatewayAccountId);
        String base64Data = gatewayInfo.getValue().getData().get(0);
        GatewayStateData gatewayState = GatewayStateData.decode(Base64Utils.INSTANCE.decode(base64Data));

        TransactionInstruction secpInstruction = RenProgram.createInstructionWithEthAddress2(
                gatewayState.renVMAuthority, renVMMessage, Arrays.copyOfRange(sig, 0, 64), sig[64] - 27);

        Transaction transaction = new Transaction();
        transaction.addInstruction(mintInstruction);
        transaction.addInstruction(secpInstruction);

        String confirmedSignature = client.getApi().sendTransaction(transaction, signer);

        return confirmedSignature;
    }

    public String findMintByDepositDetails(byte[] nHash, byte[] pHash, byte[] to, String amount) throws Exception {
        PublicKey program = resolveTokenGatewayContract();
        byte[] sHash = Hash.generateSHash();
        byte[] renVMMessage = buildRenVMMessage(pHash, amount, sHash, new PublicKey(to).asByteArray(), nHash);
        PublicKey mintLogAccount = PublicKey.Companion.findProgramAddress(Arrays.asList(Hash.keccak256(renVMMessage)), program)
                .getAddress();

        String signature = "";
        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(mintLogAccount);

            TokenProgram.MintData mintData = TokenTransaction.getMintData(accountInfo, program);
            if (!mintData.isInitialized()) {
                return signature;
            }

            List<SignatureInformationResponse> signatures = client.getApi().getConfirmedSignaturesForAddress2(mintLogAccount,
                    1);
            signature = signatures.get(0).getSignature();
        } catch (Exception e) {
        }

        return signature;
    }

    public static byte[] buildRenVMMessage(byte[] pHash, String amount, byte[] token, byte[] to, byte[] nHash) {
        ByteBuffer message = ByteBuffer.allocate(160);
        message.put(pHash);

        byte[] amountBytes = new BigInteger(amount).toByteArray();
        ByteBuffer amountBuffer = ByteBuffer.allocate(32);
        amountBuffer.position(32 - amountBytes.length);
        amountBuffer.put(amountBytes);
        message.put(amountBuffer.array());

        message.put(token);
        message.put(to);
        message.put(nHash);
        return message.array();
    }

    public BurnDetails submitBurn(PublicKey account, String amount, String recepient, Account signer) throws Exception {
        PublicKey program = resolveTokenGatewayContract();
        PublicKey tokenMint = getSPLTokenPubkey();
        PublicKey source = getAssociatedTokenAddress(account);

        TransactionInstruction checkedBurnInstruction = TokenProgram.createBurnCheckedInstruction(
                TokenProgram.PROGRAM_ID,
                tokenMint,
                source,
                account,
                new BigInteger(amount),
                8
        );

        PublicKey gatewayAccountId = PublicKey.Companion.findProgramAddress(Arrays.asList(GatewayStateKey.getBytes()), program)
                .getAddress();
        AccountInfo gatewayInfo = client.getApi().getAccountInfo(gatewayAccountId);
        String base64Data = gatewayInfo.getValue().getData().get(0);
        GatewayStateData gatewayState = GatewayStateData.decode(Base64Utils.INSTANCE.decode(base64Data));
        BigInteger nonceBN = gatewayState.burnCount.add(BigInteger.ONE);

        PublicKey burnLogAccountId = PublicKey.Companion
                .findProgramAddress(Arrays.asList(Utils.uint64ToByteArrayLE(nonceBN)), program).getAddress();

        TransactionInstruction burnInstruction = RenProgram.burnInstruction(account, source, gatewayAccountId,
                tokenMint, burnLogAccountId, recepient.getBytes(), program);

        Transaction transaction = new Transaction();
        transaction.addInstruction(checkedBurnInstruction);
        transaction.addInstruction(burnInstruction);

        String confirmedSignature = client.getApi().sendTransaction(transaction, signer);

        BurnDetails burnDetails = new BurnDetails();
        burnDetails.confirmedSignature = confirmedSignature;
        burnDetails.nonce = nonceBN;
        burnDetails.recepient = recepient;

        return burnDetails;
    }

    public static class BurnDetails {
        public String confirmedSignature;
        public BigInteger nonce;
        public String recepient;
    }

    public static class GatewayStateData extends AbstractData {
        private static final int GATEWAY_STATE_DATA_LENGTH = 1 + 20 + 32 + ByteUtils.UINT_64_LENGTH + 1;

        private boolean isInitialized;
        private byte[] renVMAuthority;
        private byte[] selectors;
        private BigInteger burnCount;
        private int underlyingDecimals;

        protected GatewayStateData(byte[] data) {
            super(data, GATEWAY_STATE_DATA_LENGTH);

            int isInitializedValue = readByte();
            isInitialized = isInitializedValue != 0;
            renVMAuthority = readBytes(20);
            selectors = readBytes(32);
            burnCount = readUint64();
            underlyingDecimals = readByte();
        }

        public static GatewayStateData decode(byte[] data) {
            return new GatewayStateData(data);
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public byte[] getRenVMAuthority() {
            return renVMAuthority;
        }

        public byte[] getSelectors() {
            return selectors;
        }

        public BigInteger getBurnCount() {
            return burnCount;
        }

        public int getUnderlyingDecimals() {
            return underlyingDecimals;
        }

    }

    public static class GatewayRegistryData extends AbstractData {
        private static final int GATEWAY_REGISTRY_DATA_LENGTH = 1 + PublicKey.PUBLIC_KEY_LENGTH
                + ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_32_LENGTH + (32 * PublicKey.PUBLIC_KEY_LENGTH)
                + ByteUtils.UINT_32_LENGTH + (32 * PublicKey.PUBLIC_KEY_LENGTH);

        private boolean isInitialized;
        private PublicKey owner;
        private int count;
        private ArrayList<String> selectors;
        private ArrayList<PublicKey> gateways;

        private GatewayRegistryData(byte[] data) {
            super(data, GATEWAY_REGISTRY_DATA_LENGTH);

            int isInitializedValue = readByte();
            isInitialized = isInitializedValue != 0;

            owner = readPublicKey();

            BigInteger countValue = readUint64();
            count = countValue.intValue();

            long selectorsSize = readUint32();
            selectors = new ArrayList<String>((int) selectorsSize);

            for (int i = 0; i < selectorsSize; i++) {
                byte[] selector = readBytes(32);
                selectors.add(Base58.encode(selector));
            }

            long gatewaysSize = readUint32();
            gateways = new ArrayList<PublicKey>((int) gatewaysSize);

            for (int i = 0; i < gatewaysSize; i++) {
                gateways.add(readPublicKey());
            }

        }

        public static GatewayRegistryData decode(byte[] data) {
            return new GatewayRegistryData(data);
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public PublicKey getOwner() {
            return owner;
        }

        public int getCount() {
            return count;
        }

        public ArrayList<String> getSelectors() {
            return selectors;
        }

        public ArrayList<PublicKey> getGateways() {
            return gateways;
        }
    }

}
