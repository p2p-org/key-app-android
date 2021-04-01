package org.p2p.solanaj.kits;

import java.math.BigInteger;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.kits.Pool.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

public class TokenSwap {


    private RpcClient client;
    private PublicKey tokenSwapAccount;
    private PublicKey swapProgramId;
    private Account payer;
    private PoolInfo poolInfo;

    public TokenSwap(RpcClient client, PublicKey tokenSwapAccount, PublicKey swapProgramId, PoolInfo poolInfo,
                     Account payer) {
        this.client = client;
        this.swapProgramId = tokenSwapAccount;
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
                poolInfo.getAuthority(), payer.getPublicKey(), amountIn);

        TransactionInstruction swap = TokenSwapProgram.swapInstruction(tokenSwapAccount, poolInfo.getAuthority(),
                tokenSource, poolInfo.getSwapData().getMintA(), poolInfo.getSwapData().getMintB(), tokenDestination,
                poolInfo.getSwapData().getTokenPool(), poolInfo.getSwapData().getFeeAccount(), null,
                TokenProgram.PROGRAM_ID, swapProgramId, amountIn, minimumAmountOut);

        Transaction transaction = new Transaction();
        transaction.addInstruction(approve);
        transaction.addInstruction(swap);

        return client.getApi().sendTransaction(transaction, payer);
    }

}
