package org.p2p.solanaj;


import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.TransactionRequest;
import org.p2p.solanaj.kits.Pool;
import org.p2p.solanaj.kits.TokenTransaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.types.TokenAccountBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

public class Swap {
    private static PublicKey WRAPPED_SOL_MINT = new PublicKey("So11111111111111111111111111111111111111112");
    static Account owner = Account.fromMnemonic(Arrays.asList("miracle", "pizza", "supply", "useful", "steak", "border",
            "same", "again", "youth", "silver", "access", "hundred"), "");
    private static PublicKey ownerPubKey = owner.getPublicKey();

    private static PublicKey findAccountAddress(RpcClient client, PublicKey tokenMint) {
        // TODO
        return new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
    }

    private static BigInteger calculateAmount(
            BigInteger tokenABalance,
            BigInteger tokenBBalance,
            double slippage,
            BigInteger inputAmoutn
    ) {
        BigInteger estimateAmount = tokenBBalance.multiply(inputAmoutn).divide(tokenABalance.add(inputAmoutn));

        return new BigDecimal(estimateAmount).multiply(BigDecimal.valueOf(1 - slippage)).toBigInteger();
    }

    public static void main(String[] args) throws Exception {
        PublicKey SWAP_PROGRAM_ID = new PublicKey("DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1");
        PublicKey poolAddress = new PublicKey("C4k1gxs9NnPhSWUbtEpnptYiduy2ZWX237gGD22N8QeC");
        double SLIPPAGE = 0.01;
        BigInteger tokenInputAmount = BigInteger.valueOf(1000000);

        ArrayList<Account> signers = new ArrayList<Account>();
        signers.add(owner);

        RpcClient client = new RpcClient(Cluster.MAINNET);
        TransactionRequest transaction = new TransactionRequest();

        Pool.PoolInfo pool = Pool.getPoolInfo(client, poolAddress);

        // swap type
        PublicKey tokenSource = pool.getSwapData().getTokenAccountA();

        PublicKey tokenA = tokenSource.equals(pool.getSwapData().getTokenAccountA())
                ? pool.getSwapData().getTokenAccountA()
                : pool.getSwapData().getTokenAccountB();
        boolean isTokenAEqTokenAccountA = tokenA.equals(pool.getSwapData().getTokenAccountA());
        PublicKey tokenB = isTokenAEqTokenAccountA ? pool.getSwapData().getTokenAccountB()
                : pool.getSwapData().getTokenAccountA();
        PublicKey mintA = isTokenAEqTokenAccountA ? pool.getSwapData().getMintA() : pool.getSwapData().getMintB();
        PublicKey mintB = isTokenAEqTokenAccountA ? pool.getSwapData().getMintB() : pool.getSwapData().getMintA();

        TokenAccountBalance tokenABalance = TokenTransaction.getTokenAccountBalance(client, tokenA);
        TokenAccountBalance tokenBBalance = TokenTransaction.getTokenAccountBalance(client, tokenB);

        BigInteger minAmountIn = calculateAmount(tokenABalance.getAmount(), tokenBBalance.getAmount(), SLIPPAGE,
                tokenInputAmount);

        TokenProgram.AccountInfoData tokenAInfo = TokenTransaction.getAccountInfoData(client, tokenA, TokenProgram.PROGRAM_ID);
        int space = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH;
        long balanceNeeded = client.getApi().getMinimumBalanceForRentExemption(space);

        PublicKey fromAccount = null;
        if (tokenAInfo.isNative()) {
            Account newAccount = new Account();
            PublicKey newAccountPubKey = newAccount.getPublicKey();

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(ownerPubKey,
                    newAccountPubKey,
                    tokenInputAmount.longValue() + balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID);

            TransactionInstruction initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccountPubKey,
                    WRAPPED_SOL_MINT,
                    ownerPubKey
            );

            transaction.addInstruction(createAccountInstruction);
            transaction.addInstruction(initializeAccountInstruction);

            signers.add(newAccount);

            fromAccount = newAccountPubKey;
        } else {
            fromAccount = findAccountAddress(client, mintA);
        }

        Account userTransferAuthority = new Account();

        PublicKey toAccount = findAccountAddress(client, mintB);
        boolean isWrappedSol = mintB.equals(WRAPPED_SOL_MINT);

        if (toAccount == null) {
            Account newAccount = new Account();
            PublicKey newAccountPubKey = newAccount.getPublicKey();

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(ownerPubKey,
                    newAccountPubKey,
                    balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID);

            TransactionInstruction initializeAccountInstruction = TokenProgram.initializeAccountInstruction(TokenProgram.PROGRAM_ID,
                    newAccountPubKey,
                    mintB,
                    ownerPubKey
            );

            transaction.addInstruction(createAccountInstruction);
            transaction.addInstruction(initializeAccountInstruction);

            signers.add(newAccount);

            toAccount = newAccountPubKey;
        }

        TransactionInstruction approveInstruction = TokenProgram.approveInstruction(TokenProgram.PROGRAM_ID,
                fromAccount,
                userTransferAuthority.getPublicKey(),
                ownerPubKey,
                tokenInputAmount);

        TransactionInstruction swapInstruction = TokenSwapProgram.swapInstruction(
                poolAddress,
                pool.getAuthority(),
                userTransferAuthority.getPublicKey(),
                fromAccount,
                tokenA,
                tokenB,
                toAccount,
                pool.getSwapData().getTokenPool(),
                pool.getSwapData().getFeeAccount(),
                pool.getSwapData().getFeeAccount(),
                TokenProgram.PROGRAM_ID,
                SWAP_PROGRAM_ID,
                tokenInputAmount,
                minAmountIn
        );

        transaction.addInstruction(approveInstruction);
        transaction.addInstruction(swapInstruction);

        boolean isNeedCloaseAccount = tokenAInfo.isNative() || isWrappedSol;
        PublicKey closeAccountPublicKey = null;
        if (tokenAInfo.isNative()) {
            closeAccountPublicKey = fromAccount;
        } else if (isWrappedSol) {
            closeAccountPublicKey = toAccount;
        }

        if (isNeedCloaseAccount && closeAccountPublicKey != null) {
            TransactionInstruction closeAccountInstruction = TokenProgram
                    .closeAccountInstruction(TokenProgram.PROGRAM_ID, closeAccountPublicKey, ownerPubKey, ownerPubKey);
            transaction.addInstruction(closeAccountInstruction);
        }
        signers.add(userTransferAuthority);

        client.getApi().sendTransaction(transaction, signers);
    }
}