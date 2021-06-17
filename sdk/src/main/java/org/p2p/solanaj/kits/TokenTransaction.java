package org.p2p.solanaj.kits;

import android.util.Base64;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.TransactionRequest;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.TokenAccountBalance;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TokenTransaction {

    public static long getMinBalanceRentForExemptMint(RpcClient client) throws RpcException {
        return client.getApi().getMinimumBalanceForRentExemption(TokenProgram.MintData.MINT_DATA_LENGTH);
    }

    public static long getMinBalanceRentForExemptAccount(RpcClient client) throws RpcException {
        return client.getApi().getMinimumBalanceForRentExemption(TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH);
    }

    public static TokenProgram.MintData getMintData(RpcClient client, PublicKey mintAddress, PublicKey programId)
            throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(mintAddress);

        if (accountInfo.getValue() == null) {
            throw new RpcException("Failed to find mint account");
        }

        if (!accountInfo.getValue().getOwner().equals(programId.toString())) {
            throw new IllegalArgumentException("Invalid mint owner");
        }

        String base64Data = accountInfo.getValue().getData().get(0);
        byte[] data = Base64.decode(base64Data, Base64.DEFAULT);

        return TokenProgram.MintData.decode(data);
    }

    public static TokenProgram.AccountInfoData getAccountInfoData(
            RpcClient client,
            PublicKey accountAddress,
            PublicKey programId
    ) throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(accountAddress);

        if (accountInfo.getValue() == null) {
            throw new RpcException("Failed to find account");
        }

        if (!accountInfo.getValue().getOwner().equals(programId.toString())) {
            throw new IllegalArgumentException("Invalid account owner");
        }

        String base64Data = accountInfo.getValue().getData().get(0);
        byte[] data = Base64.decode(base64Data, Base64.DEFAULT);

        return TokenProgram.AccountInfoData.decode(data);
    }

    public static PublicKey createAndInitializeMint(RpcClient client, Account payer, PublicKey mintAuthority,
                                                    PublicKey freezeAuthority, int decimals) throws RpcException {

        long balanceNeeded = getMinBalanceRentForExemptMint(client);
        Account newAccount = new Account();
        PublicKey newAccountPubKey = newAccount.getPublicKey();
        PublicKey payerPubKey = payer.getPublicKey();

        TransactionInstruction createAccount = SystemProgram.createAccount(payerPubKey, newAccountPubKey, balanceNeeded,
                TokenProgram.MintData.MINT_DATA_LENGTH, TokenProgram.PROGRAM_ID);

        TransactionInstruction createMint = TokenProgram.initializeMintInstruction(TokenProgram.PROGRAM_ID,
                newAccountPubKey, decimals, mintAuthority, freezeAuthority);

        TransactionRequest transaction = new TransactionRequest();
        transaction.addInstruction(createAccount);
        transaction.addInstruction(createMint);

        client.getApi().sendTransaction(transaction, Arrays.asList(payer, newAccount));

        return newAccountPubKey;
    }

    public static PublicKey createAndInitializeAccount(RpcClient client, Account payer, PublicKey mintAddress,
                                                       PublicKey owner) throws RpcException {
        long balanceNeeded = getMinBalanceRentForExemptAccount(client);

        Account newAccount = new Account();
        PublicKey newAccountPubKey = newAccount.getPublicKey();
        PublicKey payerPubKey = payer.getPublicKey();

        TransactionInstruction createAccount = SystemProgram.createAccount(payerPubKey, newAccountPubKey, balanceNeeded,
                TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH, TokenProgram.PROGRAM_ID);

        TransactionInstruction initializeAccount = TokenProgram.initializeAccountInstruction(TokenProgram.PROGRAM_ID,
                newAccountPubKey, mintAddress, owner);

        TransactionRequest transaction = new TransactionRequest();
        transaction.addInstruction(createAccount);
        transaction.addInstruction(initializeAccount);

        client.getApi().sendTransaction(transaction, Arrays.asList(payer, newAccount));

        return newAccountPubKey;
    }

    public static String mintTo(RpcClient client, Account payer, PublicKey mintAddress, PublicKey destination,
                                BigInteger amount) throws RpcException {

        TransactionInstruction mintTo = TokenProgram.mintToInstruction(TokenProgram.PROGRAM_ID, mintAddress,
                destination, payer.getPublicKey(), amount);

        TransactionRequest transaction = new TransactionRequest();
        transaction.addInstruction(mintTo);

        return client.getApi().sendTransaction(transaction, Arrays.asList(payer));
    }

    public static TokenAccountBalance getTokenAccountBalance(RpcClient client, PublicKey account) throws RpcException {
        ArrayList<Object> params = new ArrayList<Object>();

        params.add(account.toString());

        return client.call("getTokenAccountBalance", params, TokenAccountBalance.class);
    }

    public static MultipleAccountsInfo getMultipleAccounts(
            RpcClient client,
            List<PublicKey> pubkeys
    ) throws RpcException {
        ArrayList<Object> params = new ArrayList<>();

        ArrayList<String> keys = new ArrayList<>();
        for (PublicKey key : pubkeys) {
            keys.add(key.toBase58());
        }

        params.add(keys);

        HashMap<String, String> encoding = new HashMap<>();
        encoding.put("encoding", "jsonParsed");

        params.add(encoding);

        MultipleAccountsInfo result = client.call("getMultipleAccounts", params, MultipleAccountsInfo.class);

        List<AccountInfoParsed> parsedAccounts = new ArrayList<>();
        for (int i = 0; i < result.getAccountsInfoParsed().size(); i++) {
            AccountInfoParsed current = result.getAccountsInfoParsed().get(i);
            current.setAddress(keys.get(i));
            parsedAccounts.add(current);
        }

        result.setAccountsInfoParsed(parsedAccounts);

        return result;
    }

    public static TokenAccounts getTokenAccountsByOwner(RpcClient client, PublicKey owner) throws RpcException {
        return getTokenAccountsByOwner(client, owner, TokenProgram.PROGRAM_ID);
    }

    public static TokenAccounts getTokenAccountsByOwner(RpcClient client, PublicKey owner, PublicKey programId)
            throws RpcException {

        ArrayList<Object> params = new ArrayList<Object>();

        params.add(owner.toBase58());

        HashMap<String, String> programIdParam = new HashMap<String, String>();
        programIdParam.put("programId", programId.toBase58());

        params.add(programIdParam);

        HashMap<String, String> encoding = new HashMap<String, String>();
        encoding.put("encoding", "jsonParsed");

        params.add(encoding);

        return client.call("getTokenAccountsByOwner", params, TokenAccounts.class);
    }

    public static PublicKey getAssociatedTokenAddress(
            PublicKey mint,
            PublicKey owner
    ) throws Exception {
        return getAssociatedTokenAddress(TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID, TokenProgram.PROGRAM_ID, mint,
                owner);
    }

    public static PublicKey getAssociatedTokenAddress(
            PublicKey associatedProgramId,
            PublicKey programId,
            PublicKey mint,
            PublicKey owner
    ) throws Exception {
        return PublicKey
                .findProgramAddress(
                        Arrays.asList(owner.toByteArray(), programId.toByteArray(), mint.toByteArray()),
                        associatedProgramId
                )
                .getAddress();
    }

}
