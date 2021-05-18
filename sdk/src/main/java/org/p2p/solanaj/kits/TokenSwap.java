package org.p2p.solanaj.kits;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.TransactionResponse;
import org.p2p.solanaj.kits.Pool.PoolInfo;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenAccountBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;

public class TokenSwap {

    private final RpcClient client;
    private final PublicKey swapProgramId;

    public TokenSwap(RpcClient client, PublicKey swapProgramId) {
        this.client = client;
        this.swapProgramId = swapProgramId;
    }

    public String swap(
            Account owner,
            @NonNull PoolInfo pool,
            Double slippage,
            BigInteger amountIn,
            TokenAccountBalance balanceA,
            TokenAccountBalance balanceB,
            String wrappedSolMint,
            PublicKey accountAddressA,
            PublicKey accountAddressB
    ) throws RpcException {

        final ArrayList<Account> signers = new ArrayList<>(Collections.singletonList(owner));

        TransactionResponse transaction = new TransactionResponse();

        PublicKey wrappedSolAccount = new PublicKey(wrappedSolMint);

        // swap type
        PublicKey source = pool.getTokenAccountA();
        PublicKey tokenA = source.equals(pool.getTokenAccountA()) ? pool.getTokenAccountA() : pool.getTokenAccountB();

        boolean isTokenAEqTokenAccountA = tokenA.equals(pool.getTokenAccountA());

        PublicKey tokenB = isTokenAEqTokenAccountA ? pool.getTokenAccountB() : pool.getTokenAccountA();
        PublicKey mintA = isTokenAEqTokenAccountA ? pool.getMintA() : pool.getMintB();
        PublicKey mintB = isTokenAEqTokenAccountA ? pool.getMintB() : pool.getMintA();

        TokenProgram.AccountInfoData tokenAInfo = TokenTransaction.getAccountInfoData(client, tokenA, TokenProgram.PROGRAM_ID);
        int space = TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH;
        long balanceNeeded = client.getApi().getMinimumBalanceForRentExemption(space);

        PublicKey fromAccount;
        if (tokenAInfo.isNative()) {
            Account newAccount = new Account();
            PublicKey newAccountPubKey = newAccount.getPublicKey();

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                    owner.getPublicKey(),
                    newAccountPubKey,
                    amountIn.longValue() + balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID
            );

            TransactionInstruction initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccountPubKey,
                    wrappedSolAccount,
                    owner.getPublicKey()
            );

            transaction.addInstruction(createAccountInstruction);
            transaction.addInstruction(initializeAccountInstruction);

            signers.add(newAccount);

            fromAccount = newAccountPubKey;
        } else {
            fromAccount = accountAddressA;
        }

        PublicKey toAccount = accountAddressB;
        boolean isWrappedSol = mintB.equals(wrappedSolAccount);

        if (toAccount == null) {
            Account newAccount = new Account();
            PublicKey newAccountPubKey = newAccount.getPublicKey();

            TransactionInstruction createAccountInstruction = SystemProgram.createAccount(
                    owner.getPublicKey(),
                    newAccountPubKey,
                    balanceNeeded,
                    TokenProgram.AccountInfoData.ACCOUNT_INFO_DATA_LENGTH,
                    TokenProgram.PROGRAM_ID
            );

            TransactionInstruction initializeAccountInstruction = TokenProgram.initializeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    newAccountPubKey,
                    mintB,
                    owner.getPublicKey()
            );

            transaction.addInstruction(createAccountInstruction);
            transaction.addInstruction(initializeAccountInstruction);

            signers.add(newAccount);

            toAccount = newAccountPubKey;
        }

        TransactionInstruction approve = TokenProgram.approveInstruction(
                TokenProgram.PROGRAM_ID,
                fromAccount,
                pool.getAuthority(),
                owner.getPublicKey(),
                amountIn
        );

        final BigInteger estimatedAmount = TokenSwap.calculateSwapEstimatedAmount(
                balanceA,
                balanceB,
                amountIn
        );
        final BigInteger minimumAmountOut = TokenSwap.calculateSwapMinimumReceiveAmount(
                estimatedAmount,
                slippage
        );

        TransactionInstruction swap = TokenSwapProgram.swapInstruction(
                pool.getAddress(),
                pool.getAuthority(),
                fromAccount,
                tokenA,
                tokenB,
                toAccount,
                pool.getTokenPool(),
                pool.getFeeAccount(),
                pool.getFeeAccount(),
                TokenProgram.PROGRAM_ID,
                swapProgramId,
                amountIn,
                minimumAmountOut
        );

        transaction.addInstruction(approve);
        transaction.addInstruction(swap);

        boolean isNeedCloseAccount = tokenAInfo.isNative() || isWrappedSol;
        PublicKey closeAccountPublicKey = null;
        if (tokenAInfo.isNative()) {
            closeAccountPublicKey = fromAccount;
        } else if (isWrappedSol) {
            closeAccountPublicKey = toAccount;
        }

        if (isNeedCloseAccount && closeAccountPublicKey != null) {
            TransactionInstruction closeAccountInstruction = TokenProgram.closeAccountInstruction(
                    TokenProgram.PROGRAM_ID,
                    closeAccountPublicKey,
                    owner.getPublicKey(),
                    owner.getPublicKey()
            );
            transaction.addInstruction(closeAccountInstruction);
        }

        return client.getApi().sendTransaction(transaction, signers);
    }

    public static BigInteger calculateSwapMinimumReceiveAmount(BigInteger estimatedAmount, double slippage) {
        return BigDecimal.valueOf(estimatedAmount.doubleValue() * (1 - slippage)).toBigInteger();
    }

    public static BigInteger calculateSwapEstimatedAmount(TokenAccountBalance tokenABalance, TokenAccountBalance tokenBBalance, BigInteger inputAmount) {
        return tokenBBalance.getAmount().multiply(inputAmount).divide(tokenABalance.getAmount().add(inputAmount));
    }
}