package org.p2p.solanaj.programs;

import java.util.ArrayList;
import java.util.Arrays;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import static org.bitcoinj.core.Utils.*;

public class SystemProgram {
    public static final PublicKey PROGRAM_ID = new PublicKey("11111111111111111111111111111111");
    public static final PublicKey SPL_TOKEN_PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    public static final PublicKey SYSVAR_RENT_ADDRESS = new PublicKey("SysvarRent111111111111111111111111111111111");
    public static final int PROGRAM_INDEX_CREATE_ACCOUNT = 0;
    public static final int PROGRAM_INDEX_TRANSFER = 2;

    public static TransactionInstruction transfer(PublicKey fromPublicKey, PublicKey toPublickKey, long lamports) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(toPublickKey, false, true));

        // 4 byte instruction index + 8 bytes lamports
        byte[] data = new byte[4 + 8];
        uint32ToByteArrayLE(PROGRAM_INDEX_TRANSFER, data, 0);
        int64ToByteArrayLE(lamports, data, 4);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }

    public static TransactionInstruction createAccount(PublicKey fromPublicKey, PublicKey newAccountPublikkey,
            long lamports, long space, PublicKey programId) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(newAccountPublikkey, true, true));

        byte[] data = new byte[4 + 8 + 8 + 32];
        uint32ToByteArrayLE(PROGRAM_INDEX_CREATE_ACCOUNT, data, 0);
        int64ToByteArrayLE(lamports, data, 4);
        int64ToByteArrayLE(space, data, 12);
        System.arraycopy(programId.toByteArray(), 0, data, 20, 32);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }

    public static TransactionInstruction initializeAccountInstruction(PublicKey account, PublicKey mint,
                                                                      PublicKey owner) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(owner, false, false));
        keys.add(new AccountMeta(SYSVAR_RENT_ADDRESS, false, false));

        byte[] data = new byte[] { 1 };

        return new TransactionInstruction(SPL_TOKEN_PROGRAM_ID, keys, data);
    }


}
