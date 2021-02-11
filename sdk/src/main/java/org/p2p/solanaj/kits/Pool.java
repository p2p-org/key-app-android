package org.p2p.solanaj.kits;

import android.os.Build;

import androidx.annotation.NonNull;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenProgram.MintData;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.programs.TokenSwapProgram.TokenSwapData;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.ProgramSwapAccount;
import org.p2p.solanaj.rpc.types.TokenAccountBalance;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Pool {

    public static PoolInfo getPoolInfo(@NonNull RpcClient client, @NonNull ProgramSwapAccount programSwapAccount) throws RpcException {
        final String dataStr = programSwapAccount.getAccount().getData().get(0);
        final byte[] data;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data = Base64.getDecoder().decode(dataStr);
        } else {
            data = android.util.Base64.decode(dataStr, android.util.Base64.DEFAULT);
        }
        final TokenSwapData swapData = TokenSwapData.decode(data);

        final MintData tokenAInfo = Token.getMintData(client, swapData.getMintA(), swapData.getTokenProgramId());
        final MintData tokenBInfo = Token.getMintData(client, swapData.getMintB(), swapData.getTokenProgramId());
        final MintData poolTokenMint = Token.getMintData(client, swapData.getTokenPool(), swapData.getTokenProgramId());
        final TokenAccountBalance tokenABalance = Token.getTokenAccountBalance(client, swapData.getTokenAccountA());
        final TokenAccountBalance tokenBBalance = Token.getTokenAccountBalance(client, swapData.getTokenAccountB());

        final PublicKey authority = poolTokenMint.getMintAuthorityPublicKey();
        final PublicKey address = new PublicKey(programSwapAccount.getPubkey());

        return new PoolInfo(address, tokenAInfo, tokenBInfo, poolTokenMint, authority, swapData, tokenABalance, tokenBBalance);
    }

    // TODO Old one this need to understand the example in Swap.java after swap implementation - delete!!!
    public static PoolInfo getPoolInfo(RpcClient client, PublicKey address) throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(address);

        String base64Data = accountInfo.getValue().getData().get(0);
        byte[] data;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            data = Base64.getDecoder().decode(base64Data);
        } else {
            data = android.util.Base64.decode(
                    base64Data,
                    android.util.Base64.DEFAULT
            );
        }

        TokenSwapProgram.TokenSwapData swapData = TokenSwapProgram.TokenSwapData.decode(data);

        MintData tokenAInfo = Token.getMintData(client, swapData.getMintA(), TokenProgram.PROGRAM_ID);
        MintData tokenBInfo = Token.getMintData(client, swapData.getMintB(), TokenProgram.PROGRAM_ID);
        MintData poolTokenMint = Token.getMintData(client, swapData.getTokenPool(), TokenProgram.PROGRAM_ID);

        PublicKey authority = new PublicKey(poolTokenMint.getMintAuthority());

        return new PoolInfo(new PublicKey(""), tokenAInfo, tokenBInfo, poolTokenMint, authority, swapData, new TokenAccountBalance(), new TokenAccountBalance());
    }

    public static ArrayList<PoolInfo> getPools(@NonNull RpcClient client, @NonNull PublicKey publicKey) throws RpcException {

        final ArrayList<PoolInfo> pools = new ArrayList<PoolInfo>();

        final List<ProgramSwapAccount> programAccounts = client.getApi().getProgramSwapAccounts(publicKey.toBase58());
        if (programAccounts.isEmpty()) {
            return pools;
        }
        for (ProgramSwapAccount programSwapAccount : programAccounts) {
            try {
                PoolInfo poolInfo = getPoolInfo(client, programSwapAccount);
                pools.add(poolInfo);
            } catch (RpcException e) {
                e.printStackTrace();
            }
        }


        return pools;
    }

    public static class PoolInfo {
        private final String address;
        private MintData tokenAInfo;
        private MintData tokenBInfo;
        private final MintData poolTokenMint;
        private final String authority;
        private final TokenSwapData swapData;
        private TokenAccountBalance tokenABalance;
        private TokenAccountBalance tokenBBalance;

        public PoolInfo(PublicKey address, MintData tokenAInfo, MintData tokenBInfo, MintData poolTokenMint, PublicKey authority,
                        TokenSwapData swapData, TokenAccountBalance tokenABalance, TokenAccountBalance tokenBBalance) {
            this.tokenAInfo = tokenAInfo;
            this.tokenBInfo = tokenBInfo;
            this.poolTokenMint = poolTokenMint;
            this.authority = authority.toBase58();
            this.swapData = swapData;
            this.tokenABalance = tokenABalance;
            this.tokenBBalance = tokenBBalance;
            this.address = address.toBase58();
        }

        public PoolInfo swapTokenBalance() {
            final TokenAccountBalance tokenABalanceOld = tokenABalance;
            this.tokenABalance = tokenBBalance;
            this.tokenBBalance = tokenABalanceOld;
            return this;
        }

        public PoolInfo swapTokenInfo() {
            final MintData tokenAInfoOld = tokenAInfo;
            this.tokenAInfo = tokenBInfo;
            this.tokenBInfo = tokenAInfoOld;
            return this;
        }

        public MintData getTokenAInfo() {
            return tokenAInfo;
        }

        public MintData getTokenBInfo() {
            return tokenBInfo;
        }

        public MintData getPoolTokenMint() {
            return poolTokenMint;
        }

        public PublicKey getAuthority() {
            return new PublicKey(authority);
        }

        public TokenSwapData getSwapData() {
            return swapData;
        }

        public PublicKey getAddress() {
            return new PublicKey(address);
        }

        public TokenAccountBalance getTokenABalance() {
            return tokenABalance;
        }

        public TokenAccountBalance getTokenBBalance() {
            return tokenBBalance;
        }
    }
}