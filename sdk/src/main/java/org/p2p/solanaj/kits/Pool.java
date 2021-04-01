package org.p2p.solanaj.kits;

import java.util.Base64;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.programs.TokenProgram.MintData;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.programs.TokenSwapProgram.TokenSwapData;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;


public class Pool {

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

        PublicKey authority = poolTokenMint.getMintAuthority();

        return new PoolInfo(tokenAInfo, tokenBInfo, poolTokenMint, authority, swapData);
    }

    public static class PoolInfo {
        private MintData tokenAInfo;
        private MintData tokenBInfo;
        private MintData poolTokenMint;
        private PublicKey authority;
        private TokenSwapData swapData;;

        public PoolInfo(MintData tokenAInfo, MintData tokenBInfo, MintData poolTokenMint, PublicKey authority,
                        TokenSwapData swapData) {
            this.tokenAInfo = tokenAInfo;
            this.tokenBInfo = tokenBInfo;
            this.poolTokenMint = poolTokenMint;
            this.authority = authority;
            this.swapData = swapData;
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
            return authority;
        }

        public TokenSwapData getSwapData() {
            return swapData;
        }
    }

}
