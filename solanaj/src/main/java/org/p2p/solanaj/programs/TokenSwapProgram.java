package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AbstractData;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.utils.ByteUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class TokenSwapProgram {
    public static final int INSTRUCTION_INDEX_INITIALIZE_SWAP = 0;
    public static final int INSTRUCTION_INDEX_SWAP = 1;
    public static final int INSTRUCTION_INDEX_DEPOSIT = 2;
    public static final int INSTRUCTION_INDEX_WITHDRAW = 3;

    public static String getSwapProgramId(int version) {
        if (version == 2) {
            return "9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP";
        } else {
            return "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1";
        }
    }

    public static TransactionInstruction initializeSwapInstruction(PublicKey tokenSwapAccount, PublicKey authority,
                                                                   PublicKey tokenAccountA, PublicKey tokenAccountB, PublicKey tokenPool, PublicKey feeAccount,
                                                                   PublicKey tokenAccountPool, PublicKey tokenProgramId, PublicKey swapProgramId, int nonce, int curveType,
                                                                   BigInteger tradeFeeNumerator, BigInteger tradeFeeDenominator, BigInteger ownerTradeFeeNumerator,
                                                                   BigInteger ownerTradeFeeDenominator, BigInteger ownerWithdrawFeeNumerator,
                                                                   BigInteger ownerWithdrawFeeDenominator, BigInteger hostFeeNumerator, BigInteger hostFeeDenominator) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(tokenSwapAccount, false, true));
        keys.add(new AccountMeta(authority, false, false));
        keys.add(new AccountMeta(tokenAccountA, false, false));
        keys.add(new AccountMeta(tokenAccountB, false, false));
        keys.add(new AccountMeta(tokenPool, false, true));
        keys.add(new AccountMeta(feeAccount, false, false));
        keys.add(new AccountMeta(tokenAccountPool, false, true));
        keys.add(new AccountMeta(tokenProgramId, false, false));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_INITIALIZE_SWAP);
        bos.write(nonce);

        try {
            ByteUtils.uint64ToByteStreamLE(tradeFeeNumerator, bos);
            ByteUtils.uint64ToByteStreamLE(tradeFeeDenominator, bos);
            ByteUtils.uint64ToByteStreamLE(ownerTradeFeeNumerator, bos);
            ByteUtils.uint64ToByteStreamLE(ownerTradeFeeDenominator, bos);
            ByteUtils.uint64ToByteStreamLE(ownerWithdrawFeeNumerator, bos);
            ByteUtils.uint64ToByteStreamLE(ownerWithdrawFeeDenominator, bos);
            ByteUtils.uint64ToByteStreamLE(hostFeeNumerator, bos);
            ByteUtils.uint64ToByteStreamLE(hostFeeDenominator, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        bos.write(curveType);
        try {
            // todo: it was bos.writeBytes() workaround
            bos.write(new byte[32]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TransactionInstruction(swapProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction swapInstruction(PublicKey tokenSwapAccount, PublicKey authority,
                                                         PublicKey userTransferAuthority, PublicKey userSource, PublicKey poolSource, PublicKey poolDestination,
                                                         PublicKey userDestination, PublicKey poolMint, PublicKey feeAccount, PublicKey hostFeeAccount,
                                                         PublicKey tokenProgramId, PublicKey swapProgramId, BigInteger amountIn, BigInteger minimumAmountOut) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(tokenSwapAccount, false, false));
        keys.add(new AccountMeta(authority, false, false));
        keys.add(new AccountMeta(userTransferAuthority, true, false));
        keys.add(new AccountMeta(userSource, false, true));
        keys.add(new AccountMeta(poolSource, false, true));
        keys.add(new AccountMeta(poolDestination, false, true));
        keys.add(new AccountMeta(userDestination, false, true));
        keys.add(new AccountMeta(poolMint, false, true));
        keys.add(new AccountMeta(feeAccount, false, true));
        keys.add(new AccountMeta(tokenProgramId, false, false));

        if (hostFeeAccount != null) {
            keys.add(new AccountMeta(hostFeeAccount, false, true));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_SWAP);

        try {
            ByteUtils.uint64ToByteStreamLE(amountIn, bos);
            ByteUtils.uint64ToByteStreamLE(minimumAmountOut, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(swapProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction depositInstruction(PublicKey tokenSwap, PublicKey authority,
                                                            PublicKey userTransferAuthority, PublicKey sourceA, PublicKey sourceB, PublicKey intoA, PublicKey intoB,
                                                            PublicKey poolToken, PublicKey poolAccount, PublicKey tokenProgramId, PublicKey swapProgramId,
                                                            BigInteger poolTokenAmount, BigInteger maximumTokenA, BigInteger maximumTokenB) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(tokenSwap, false, false));
        keys.add(new AccountMeta(authority, false, false));
        keys.add(new AccountMeta(userTransferAuthority, true, false));
        keys.add(new AccountMeta(sourceA, false, true));
        keys.add(new AccountMeta(sourceB, false, true));
        keys.add(new AccountMeta(intoA, false, true));
        keys.add(new AccountMeta(intoB, false, true));
        keys.add(new AccountMeta(poolToken, false, true));
        keys.add(new AccountMeta(poolAccount, false, true));
        keys.add(new AccountMeta(tokenProgramId, false, true));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_DEPOSIT);

        try {
            ByteUtils.uint64ToByteStreamLE(poolTokenAmount, bos);
            ByteUtils.uint64ToByteStreamLE(maximumTokenA, bos);
            ByteUtils.uint64ToByteStreamLE(maximumTokenB, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(swapProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction withdrawInstruction(PublicKey tokenSwap, PublicKey authority,
                                                             PublicKey userTransferAuthority, PublicKey poolMint, PublicKey feeAccount, PublicKey sourcePoolAccount,
                                                             PublicKey fromA, PublicKey fromB, PublicKey userAccountA, PublicKey userAccountB, PublicKey swapProgramId,
                                                             PublicKey tokenProgramId, BigInteger poolTokenAmount, BigInteger minimumTokenA, BigInteger minimumTokenB) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(tokenSwap, false, false));
        keys.add(new AccountMeta(authority, false, false));
        keys.add(new AccountMeta(userTransferAuthority, true, false));
        keys.add(new AccountMeta(poolMint, false, true));
        keys.add(new AccountMeta(sourcePoolAccount, false, true));
        keys.add(new AccountMeta(fromA, false, true));
        keys.add(new AccountMeta(fromB, false, true));
        keys.add(new AccountMeta(userAccountA, false, true));
        keys.add(new AccountMeta(userAccountB, false, true));
        keys.add(new AccountMeta(feeAccount, false, false));
        keys.add(new AccountMeta(tokenProgramId, false, false));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_WITHDRAW);

        try {
            ByteUtils.uint64ToByteStreamLE(poolTokenAmount, bos);
            ByteUtils.uint64ToByteStreamLE(minimumTokenA, bos);
            ByteUtils.uint64ToByteStreamLE(minimumTokenB, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(swapProgramId, keys, bos.toByteArray());
    }

    public static class TokenSwapData extends AbstractData {
        public static final int TOKEN_SWAP_DATA_LENGTH = 1 + 1 + 1 + 7 * PublicKey.PUBLIC_KEY_LENGTH
                + 8 * ByteUtils.UINT_64_LENGTH + 1 + 32;

        private int version;
        private boolean isInitialized;
        private int nonce;
        private PublicKey tokenProgramId;
        private PublicKey tokenAccountA;
        private PublicKey tokenAccountB;
        private PublicKey tokenPool;
        private PublicKey mintA;
        private PublicKey mintB;
        private PublicKey feeAccount;
        private BigInteger tradeFeeNumerator;
        private BigInteger tradeFeeDenominator;
        private BigInteger ownerTradeFeeNumerator;
        private BigInteger ownerTradeFeeDenominator;
        private BigInteger ownerWithdrawFeeNumerator;
        private BigInteger ownerWithdrawFeeDenominator;
        private BigInteger hostFeeNumerator;
        private BigInteger hostFeeDenominator;
        private int curveType;
        private byte[] curveParameters;

        private TokenSwapData(byte[] data) {
            super(data, TOKEN_SWAP_DATA_LENGTH);

            version = readByte();
            isInitialized = readByte() == 1;
            nonce = readByte();
            tokenProgramId = readPublicKey();
            tokenAccountA = readPublicKey();
            tokenAccountB = readPublicKey();
            tokenPool = readPublicKey();
            mintA = readPublicKey();
            mintB = readPublicKey();
            feeAccount = readPublicKey();
            tradeFeeNumerator = readUint64();
            tradeFeeDenominator = readUint64();
            ownerTradeFeeNumerator = readUint64();
            ownerTradeFeeDenominator = readUint64();
            ownerWithdrawFeeNumerator = readUint64();
            ownerWithdrawFeeDenominator = readUint64();
            hostFeeNumerator = readUint64();
            hostFeeDenominator = readUint64();
            curveType = readByte();
            curveParameters = readBytes(32);
        }

        public TokenSwapData swapMintData() {
            PublicKey mintAOld = mintA;
            mintA = mintB;
            mintB = mintAOld;
            return this;
        }

        public TokenSwapData swapTokenAccount() {
            PublicKey tokenAccountAOld = tokenAccountA;
            tokenAccountA = tokenAccountB;
            tokenAccountB = tokenAccountAOld;
            return this;
        }

        public static TokenSwapData decode(byte[] data) {
            return new TokenSwapData(data);
        }

        public int getVersion() {
            return version;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public int getNonce() {
            return nonce;
        }

        public PublicKey getTokenProgramId() {
            return tokenProgramId;
        }

        public PublicKey getTokenAccountA() {
            return tokenAccountA;
        }

        public PublicKey getTokenAccountB() {
            return tokenAccountB;
        }

        public PublicKey getTokenPool() {
            return tokenPool;
        }

        public PublicKey getMintA() {
            return mintA;
        }

        public PublicKey getMintB() {
            return mintB;
        }

        public PublicKey getFeeAccount() {
            return feeAccount;
        }

        public int getCurveType() {
            return curveType;
        }

        public BigInteger getTradeFeeNumerator() {
            return tradeFeeNumerator;
        }

        public BigInteger getTradeFeeDenominator() {
            return tradeFeeDenominator;
        }

        public BigInteger getOwnerTradeFeeNumerator() {
            return ownerTradeFeeNumerator;
        }

        public BigInteger getOwnerTradeFeeDenominator() {
            return ownerTradeFeeDenominator;
        }

        public BigInteger getOwnerWithdrawFeeNumerator() {
            return ownerWithdrawFeeNumerator;
        }

        public BigInteger getOwnerWithdrawFeeDenominator() {
            return ownerWithdrawFeeDenominator;
        }

        public BigInteger getHostFeeNumerator() {
            return hostFeeNumerator;
        }

        public BigInteger getHostFeeDenominator() {
            return hostFeeDenominator;
        }

        public byte[] getCurveParameters() {
            return curveParameters;
        }

    }

}
