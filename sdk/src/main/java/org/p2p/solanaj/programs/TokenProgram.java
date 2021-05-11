package org.p2p.solanaj.programs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.p2p.solanaj.core.AbstractData;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.core.Sysvar;

public class TokenProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

    public static final int INSTRUCTION_INDEX_INITIALIZE_MINT = 0;
    public static final int INSTRUCTION_INDEX_INITIALIZE_ACCOUNT = 1;
    public static final int INSTRUCTION_INDEX_TRANSFER = 3;
    public static final int INSTRUCTION_INDEX_APPROVE = 4;
    public static final int INSTRUCTION_INDEX_MINT_TO = 7;
    public static final int INSTRUCTION_INDEX_CLOSE_ACCOUNT = 9;

    public static TransactionInstruction initializeMintInstruction(PublicKey tokenProgramId, PublicKey mint,
                                                                   int decimals, PublicKey authority, PublicKey freezeAuthority) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(mint, false, true));
        keys.add(new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_INITIALIZE_MINT);
        bos.write(decimals);
       // bos.writeBytes(authority.toByteArray());
        bos.write(authority.toByteArray(), 0, authority.toByteArray().length);
        bos.write(freezeAuthority == null ? 0 : 1);
        //bos.writeBytes(freezeAuthority != null ? freezeAuthority.toByteArray() : new byte[PublicKey.PUBLIC_KEY_LENGTH]);
        if (freezeAuthority != null) {
            bos.write(freezeAuthority.toByteArray(), 0, freezeAuthority.toByteArray().length);
        } else {
            byte[] publicKeyArray = new byte[PublicKey.PUBLIC_KEY_LENGTH];
            bos.write(publicKeyArray, 0, publicKeyArray.length);
        }
        return new TransactionInstruction(tokenProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction initializeAccountInstruction(PublicKey tokenProgramId, PublicKey account,
                                                                      PublicKey mint, PublicKey owner) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(owner, false, false));
        keys.add(new AccountMeta(Sysvar.SYSVAR_RENT_ADDRESS, false, false));

        byte[] data = new byte[]{INSTRUCTION_INDEX_INITIALIZE_ACCOUNT};

        return new TransactionInstruction(tokenProgramId, keys, data);
    }

    public static TransactionInstruction transferInstruction(PublicKey tokenProgramId, PublicKey source,
                                                             PublicKey destination, PublicKey owner, BigInteger amount) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(source, false, true));
        keys.add(new AccountMeta(destination, false, true));
        keys.add(new AccountMeta(owner, true, true));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_TRANSFER);

        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(tokenProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction approveInstruction(PublicKey tokenProgramId, PublicKey account,
                                                            PublicKey delegate, PublicKey owner, BigInteger amount) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(delegate, false, false));
        keys.add(new AccountMeta(owner, true, true));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_APPROVE);

        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(tokenProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction mintToInstruction(PublicKey tokenProgramId, PublicKey mint,
                                                           PublicKey destination, PublicKey authority, BigInteger amount) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(mint, false, true));
        keys.add(new AccountMeta(destination, false, true));
        keys.add(new AccountMeta(authority, true, true));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(INSTRUCTION_INDEX_MINT_TO);

        try {
            ByteUtils.uint64ToByteStreamLE(amount, bos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new TransactionInstruction(tokenProgramId, keys, bos.toByteArray());
    }

    public static TransactionInstruction closeAccountInstruction(PublicKey tokenProgramId, PublicKey account,
                                                                 PublicKey destination, PublicKey owner) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(destination, false, true));
        keys.add(new AccountMeta(owner, false, false));

        byte[] data = new byte[]{INSTRUCTION_INDEX_CLOSE_ACCOUNT};

        return new TransactionInstruction(tokenProgramId, keys, data);
    }

    public static class MintData extends AbstractData {
        public static final int MINT_DATA_LENGTH = ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH
                + ByteUtils.UINT_64_LENGTH + 1 + 1 + ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH;

        private long mintAuthorityOption;
        private String mintAuthority;
        private BigInteger supply;
        private int decimals;
        private boolean isInitialized;
        private long freezeAuthorityOption;
        private PublicKey freezeAuthority;

        private MintData(byte[] data) {
            super(data, MINT_DATA_LENGTH);

            mintAuthorityOption = readUint32();
            mintAuthority = readPublicKey().toBase58();
            supply = readUint64();
            decimals = readByte();
            isInitialized = readByte() != 0;
            freezeAuthorityOption = readUint32();
            freezeAuthority = readPublicKey();

            if (mintAuthorityOption == 0) {
                mintAuthority = null;
            }

            if (freezeAuthorityOption == 0) {
                freezeAuthority = null;
            }
        }

        public static MintData decode(byte[] data) {
            return new MintData(data);
        }

        public String getMintAuthority() {
            return mintAuthority;
        }

        public PublicKey getMintAuthorityPublicKey() {
            return new PublicKey(mintAuthority);
        }

        public BigInteger getSupply() {
            return supply;
        }

        public int getDecimals() {
            return decimals;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public PublicKey getFreezeAuthority() {
            return freezeAuthority;
        }

    }

    public static class AccountInfoData extends AbstractData {
        public static final int ACCOUNT_INFO_DATA_LENGTH = PublicKey.PUBLIC_KEY_LENGTH + PublicKey.PUBLIC_KEY_LENGTH
                + ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH + 1
                + ByteUtils.UINT_32_LENGTH + ByteUtils.UINT_64_LENGTH + ByteUtils.UINT_64_LENGTH
                + ByteUtils.UINT_32_LENGTH + PublicKey.PUBLIC_KEY_LENGTH;

        private PublicKey mint;
        private PublicKey owner;
        private BigInteger amount;
        private long delegateOption;
        private PublicKey delegate;
        private boolean isInitialized;
        private boolean isFrozen;
        private int state;
        private long isNativeOption;
        private BigInteger rentExemptReserve;
        private BigInteger isNativeRaw;
        private boolean isNative;
        private BigInteger delegatedAmount;
        private long closeAuthorityOption;
        private PublicKey closeAuthority;

        private AccountInfoData(byte[] data) {
            super(data, ACCOUNT_INFO_DATA_LENGTH);

            mint = readPublicKey();
            owner = readPublicKey();
            amount = readUint64();
            delegateOption = readUint32();
            delegate = readPublicKey();
            state = readByte();
            isNativeOption = readUint32();
            isNativeRaw = readUint64();
            delegatedAmount = readUint64();
            closeAuthorityOption = readUint32();
            closeAuthority = readPublicKey();

            if (delegateOption == 0) {
                delegate = null;
                delegatedAmount = BigInteger.valueOf(0);
            }

            isInitialized = state != 0;
            isFrozen = state == 2;

            if (isNativeOption == 1) {
                rentExemptReserve = isNativeRaw;
                isNative = true;
            } else {
                rentExemptReserve = null;
                isNative = false;
            }

            if (closeAuthorityOption == 0) {
                closeAuthority = null;
            }
        }

        public static AccountInfoData decode(byte[] data) {
            return new AccountInfoData(data);
        }

        public PublicKey getMint() {
            return mint;
        }

        public PublicKey getOwner() {
            return owner;
        }

        public BigInteger getAmount() {
            return amount;
        }

        public PublicKey getDelegate() {
            return delegate;
        }

        public boolean isNative() {
            return isNative;
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        public boolean isFrozen() {
            return isFrozen;
        }

        public BigInteger getRentExemptReserve() {
            return rentExemptReserve;
        }

        public BigInteger getDelegatedAmount() {
            return delegatedAmount;
        }

        public PublicKey getCloseAuthority() {
            return closeAuthority;
        }
    }

}
