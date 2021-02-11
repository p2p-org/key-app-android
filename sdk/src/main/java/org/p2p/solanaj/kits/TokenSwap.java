package org.p2p.solanaj.kits;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.kits.Pool.PoolInfo;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenAccountBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

public class TokenSwap {

    private final RpcClient client;
    private final PublicKey swapProgramId;

    public TokenSwap(RpcClient client, PublicKey swapProgramId) {
        this.client = client;
        this.swapProgramId = swapProgramId;
    }

    public String swap(
            Account owner,
            PoolInfo pool,
            PublicKey source,
//            PublicKey sourceMint,
            PublicKey destination,
//            PublicKey destinationMint,
            Double slippage,
            BigInteger amountIn) throws RpcException {

        if (pool == null) {
            throw new RuntimeException("Pool doesn't exsist");
        }

        final ArrayList<Account> signers = new ArrayList<Account>(Collections.singletonList(owner));

        TransactionInstruction approve = TokenProgram.approveInstruction(
                TokenProgram.PROGRAM_ID,
                source, pool.getAuthority(), owner.getPublicKey(), amountIn
        );

        final BigInteger estimatedAmount = TokenSwap.calculateSwapEstimatedAmount(
                pool.getTokenABalance(),
                pool.getTokenBBalance(),
                amountIn
        );
        final BigInteger minimumAmountOut = TokenSwap.calculateSwapMinimumReceiveAmount(
                estimatedAmount,
                slippage
        );

        TransactionInstruction swap = TokenSwapProgram.swapInstruction(
                pool.getAddress(),
                pool.getAuthority(),
                source,
                pool.getSwapData().getTokenAccountA(),
                pool.getSwapData().getTokenAccountB(),
                destination,
                pool.getSwapData().getTokenPool(),
                pool.getSwapData().getFeeAccount(),
                null,
                TokenProgram.PROGRAM_ID,
                swapProgramId,
                minimumAmountOut,
                amountIn
        );

        Transaction transaction = new Transaction();
        transaction.addInstruction(approve);
        transaction.addInstruction(swap);

        return client.getApi().sendTransaction(transaction, signers);
    }


    public static BigInteger calculateSwapMinimumReceiveAmount(BigInteger estimatedAmount, double slippage) {
        return BigDecimal.valueOf(estimatedAmount.doubleValue() * (1 - slippage)).toBigInteger();
    }

    public static BigInteger calculateSwapEstimatedAmount(TokenAccountBalance tokenABalance, TokenAccountBalance tokenBBalance, BigInteger inputAmount) {
        return tokenBBalance.getAmount().multiply(inputAmount).divide(tokenABalance.getAmount().add(inputAmount));
    }

}