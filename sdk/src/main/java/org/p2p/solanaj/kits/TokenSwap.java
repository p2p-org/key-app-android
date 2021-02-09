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

public class TokenSwap {

    private RpcClient client;
    private PublicKey tokenSwapAccount;
    private PublicKey swapProgramId;
    private Account payer;
    private PoolInfo poolInfo;

    public TokenSwap(RpcClient client, PublicKey tokenSwapAccount, PublicKey swapProgramId, PoolInfo poolInfo,
                     Account payer) {
        this.client = client;
        this.tokenSwapAccount = tokenSwapAccount;
        this.swapProgramId = swapProgramId;
        this.poolInfo = poolInfo;
        this.payer = payer;
    }

    public String swap(PublicKey tokenSource, PublicKey tokenDestination, BigInteger amountIn,
                       BigInteger minimumAmountOut) throws RpcException {

        if (poolInfo == null) {
            throw new RuntimeException("Pool doesn't exsist");
        }

        TransactionInstruction approve = TokenProgram.approveInstruction(TokenProgram.PROGRAM_ID, tokenSource,
                new PublicKey(poolInfo.getAuthority()), payer.getPublicKey(), amountIn);

        TransactionInstruction swap = TokenSwapProgram.swapInstruction(tokenSwapAccount, new PublicKey(poolInfo.getAuthority()),
                tokenSource, poolInfo.getSwapData().getTokenAccountA(), poolInfo.getSwapData().getTokenAccountB(), tokenDestination,
                poolInfo.getSwapData().getTokenPool(), poolInfo.getSwapData().getFeeAccount(), poolInfo.getSwapData().getFeeAccount(),
                TokenProgram.PROGRAM_ID, swapProgramId, amountIn, minimumAmountOut);

        Transaction transaction = new Transaction();
        transaction.addInstruction(approve);
        transaction.addInstruction(swap);

        return client.getApi().sendTransaction(transaction, payer);
    }

    public static BigInteger calculateSwapMinimumReceiveAmount(BigInteger estimatedAmount, double slippage) {
        return BigDecimal.valueOf(estimatedAmount.doubleValue() * (1 - slippage) / 100).toBigInteger();
    }

    public static BigInteger calculateSwapEstimatedAmount(TokenAccountBalance tokenABalance, TokenAccountBalance tokenBBalance, BigInteger inputAmount) {
        return tokenBBalance.getAmount().multiply(inputAmount).divide(tokenABalance.getAmount().add(inputAmount));
    }

}
