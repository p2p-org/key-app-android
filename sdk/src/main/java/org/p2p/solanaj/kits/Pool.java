package org.p2p.solanaj.kits;

import android.util.Base64;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.programs.TokenSwapProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.AccountInfo;
import org.p2p.solanaj.rpc.types.ProgramAccount;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pool {

    public static PoolInfo getPoolInfo(RpcClient client, PublicKey address) throws RpcException {
        AccountInfo accountInfo = client.getApi().getAccountInfo(address);

        String base64Data = accountInfo.getValue().getData().get(0);
        byte[] decode = Base64.decode(base64Data, Base64.DEFAULT);
        TokenSwapProgram.TokenSwapData swapData = TokenSwapProgram.TokenSwapData.decode(decode);

        return new PoolInfo(address, new PublicKey(accountInfo.getValue().getOwner()), swapData);
    }

    public static List<PoolInfo> getPools(RpcClient client, PublicKey swapProgramId) throws RpcException {
        List<ProgramAccount> programAccounts = client.getApi().getProgramAccounts(swapProgramId);

        List<PoolInfo> result = new ArrayList<>();

        for (ProgramAccount programAccount : programAccounts) {
            result.add(PoolInfo.fromProgramAccount(programAccount));
        }

        return result;
    }

    public static class PoolInfo {
        private PublicKey address;
        private PublicKey swapProgramId;
        private TokenSwapProgram.TokenSwapData swapData;

        public PoolInfo(PublicKey address, PublicKey swapProgramId, TokenSwapProgram.TokenSwapData swapData) {
            this.address = address;
            this.swapProgramId = swapProgramId;
            this.swapData = swapData;
        }

        public static PoolInfo fromProgramAccount(ProgramAccount programAccount) {
            return new PoolInfo(new PublicKey(programAccount.getPubkey()),
                    new PublicKey(programAccount.getAccount().getOwner()),
                    TokenSwapProgram.TokenSwapData.decode(programAccount.getAccount().getDecodedData())
            );
        }

        public PublicKey getAddress() {
            return address;
        }

        public PublicKey getSwapProgramId() {
            return swapProgramId;
        }

        public TokenSwapProgram.TokenSwapData getSwapData() {
            return swapData;
        }

        public PublicKey getAuthority() {
            PublicKey authority;
            try {
                authority = PublicKey.findProgramAddress(Arrays.asList(address.toByteArray()), swapProgramId)
                        .getAddress();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return authority;
        }

        public PublicKey getTokenProgramId() {
            return swapData.getTokenProgramId();
        }

        public PublicKey getTokenAccountA() {
            return swapData.getTokenAccountA();
        }

        public PublicKey getTokenAccountB() {
            return swapData.getTokenAccountB();
        }

        public PublicKey getTokenPool() {
            return swapData.getTokenPool();
        }

        public PublicKey getMintA() {
            return swapData.getMintA();
        }

        public PublicKey getMintB() {
            return swapData.getMintB();
        }

        public PublicKey getFeeAccount() {
            return swapData.getFeeAccount();
        }

        public BigInteger getTradeFeeNumerator() {
            return swapData.getTradeFeeNumerator();
        }

        public BigInteger getTradeFeeDenominator() {
            return swapData.getTradeFeeDenominator();
        }

    }
}