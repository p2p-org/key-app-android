package org.p2p.solanaj;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.Environment;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class CreateAndInitializeAccount {
    public static final PublicKey SPL_TOKEN_PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
    public static final PublicKey SYSVAR_RENT_ADDRESS = new PublicKey("SysvarRent111111111111111111111111111111111");

    public static TransactionInstruction initializeAccountInstruction(PublicKey account, PublicKey mint, PublicKey owner) {
        ArrayList<AccountMeta> keys = new ArrayList<AccountMeta>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(owner, false, false));
        keys.add(new AccountMeta(SYSVAR_RENT_ADDRESS, false, false));
        byte[] data = new byte[]{1};
        return new TransactionInstruction(SPL_TOKEN_PROGRAM_ID, keys, data);
    }
/*

    public static String createAndInitializeTokenAccount(RpcClient client, Account payer, PublicKey mintAddress, Account newAccount) throws RpcException {
        Integer space = 32 + 32 + 8 + 93;
        // mint account data length: 32 + 32 + 8 + 93
        PublicKey newAccountPubKey = newAccount.getPublicKey();
        PublicKey payerPubKey = payer.getPublicKey();
        long minBalance = client.getApi().getMinimumBalanceForRentExemption(space);
        TransactionInstruction createAccount = SystemProgram.createAccount(payerPubKey, newAccountPubKey, minBalance, space, SPL_TOKEN_PROGRAM_ID);
        TransactionInstruction initializeAccount = initializeAccountInstruction(newAccountPubKey, mintAddress, payerPubKey);
        Transaction transaction = new Transaction();
        transaction.addInstruction(createAccount);
        transaction.addInstruction(initializeAccount);
        return client.getApi().sendTransaction(transaction, Arrays.asList(payer, newAccount));
    }
*/

    public static void main(String[] args) throws RpcException {
//        RpcClient client = new RpcClient(Environment.MAINNET);
     /*   Account payer = new Account( < secret key >);
        PublicKey mintAddress = new PublicKey( < mint address >);
        Account newAccount = new Account();
        String signature = createAndInitializeTokenAccount(client, payer, mintAddress, newAccount);
        System.out.print(signature);*/
    }
}