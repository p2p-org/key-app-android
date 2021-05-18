package org.p2p.solanaj;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.TransactionResponse;
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

public class Swap {
    private static PublicKey WRAPPED_SOL_MINT = new PublicKey("So11111111111111111111111111111111111111112");
    private static Account owner = new Account(
            Base58.decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));
    private static PublicKey ownerPubKey = owner.getPublicKey();

    private static PublicKey findAccountAddress(PublicKey tokenMint) {
        // TODO
        return new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
    }

    private static BigInteger calculateAmount(BigInteger tokenABalance, BigInteger tokenBBalance, double slippage,
                                              BigInteger inputAmoutn) {
        BigInteger estimateAmount = tokenBBalance.multiply(inputAmoutn).divide(tokenABalance.add(inputAmoutn));

        return new BigDecimal(estimateAmount).multiply(BigDecimal.valueOf(1 - slippage)).toBigInteger();
    }

    public static void main(String[] args) throws Exception {
        PublicKey SWAP_PROGRAM_ID = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");

        PublicKey poolAddress = new PublicKey("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");
        double SLIPPAGE = 0.01;
        BigInteger tokenInputAmount = BigInteger.valueOf(1000000);

        ArrayList<Account> signers = new ArrayList<Account>();
        signers.add(owner);

        RpcClient client = new RpcClient(Cluster.MAINNET);
        TransactionResponse transaction = new TransactionResponse();

        Pool.PoolInfo pool = Pool.getPoolInfo(client, poolAddress);

        // swap type
        PublicKey tokenSource = pool.getTokenAccountA();

        PublicKey tokenA = tokenSource.equals(pool.getTokenAccountA())
                ? pool.getTokenAccountA()
                : pool.getTokenAccountB();
        boolean isTokenAEqTokenAccountA = tokenA.equals(pool.getTokenAccountA());
        PublicKey tokenB = isTokenAEqTokenAccountA ? pool.getTokenAccountB()
                : pool.getTokenAccountA();
        PublicKey mintA = isTokenAEqTokenAccountA ? pool.getMintA() : pool.getMintB();
        PublicKey mintB = isTokenAEqTokenAccountA ? pool.getMintB() : pool.getMintA();

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

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                    ownerPubKey,
                    newAccountPubKey,
                    tokenInputAmount.longValue() + balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID
            );

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
            fromAccount = findAccountAddress(mintA);
        }

        PublicKey toAccount = findAccountAddress(mintB);
        boolean isWrappedSol = mintB.equals(WRAPPED_SOL_MINT);

        if (toAccount == null) {
            Account newAccount = new Account();
            PublicKey newAccountPubKey = newAccount.getPublicKey();

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                    ownerPubKey,
                    newAccountPubKey,
                    balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID
            );

            TransactionInstruction initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccountPubKey,
                    mintB,
                    ownerPubKey
            );

            transaction.addInstruction(createAccountInstruction);
            transaction.addInstruction(initializeAccountInstruction);

            signers.add(newAccount);

            toAccount = newAccountPubKey;
        }

        TransactionInstruction approveInstruction = TokenProgram.approveInstruction(
                TokenProgram.PROGRAM_ID,
                fromAccount,
                pool.getAuthority(),
                ownerPubKey,
                tokenInputAmount
        );

        TransactionInstruction swapInstruction = TokenSwapProgram.swapInstruction(
                poolAddress,
                pool.getAuthority(),
                fromAccount,
                tokenA,
                tokenB,
                toAccount,
                pool.getTokenPool(),
                pool.getFeeAccount(),
                pool.getFeeAccount(),
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
            TransactionInstruction closeAccountInstruction = TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    closeAccountPublicKey,
                    ownerPubKey,
                    ownerPubKey
            );
            transaction.addInstruction(closeAccountInstruction);
        }

        client.getApi().sendTransaction(transaction, signers);
    }

}
