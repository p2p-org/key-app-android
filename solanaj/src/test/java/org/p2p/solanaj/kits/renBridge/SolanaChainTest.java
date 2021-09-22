package org.p2p.solanaj.kits.renBridge;

import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.utils.Utils;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class SolanaChainTest {

    @Test
    public void gatewayRegistryStateKeyTest() throws Exception {
        NetworkConfig networkConfig = NetworkConfig.DEVNET();

        PublicKey pubk = new PublicKey(networkConfig.getGatewayRegistry());
        assertTrue(new PublicKey("REGrPFKQhRneFFdUV3e9UDdzqUJyS6SKj88GdXFCRd2").equals(pubk));

        PublicKey stateKey = PublicKey.Companion
                .findProgramAddress(Arrays.asList(SolanaChain.GatewayRegistryStateKey.getBytes()), pubk).getAddress();
        assertTrue(new PublicKey("4aMET2gUF29qk8G4Zbg2bWxLkFaTWuTYqnvQqFY16J6c").equals(stateKey));
    }

    @Test
    public void decodeGatewayRegistryDataTest() {
        SolanaChain.GatewayRegistryData gatewayRegistryData = SolanaChain.GatewayRegistryData
                .decode(Base64.getDecoder().decode(Mock.base64GatewayRegistryData));

        assertTrue(gatewayRegistryData.isInitialized());
        assertTrue(
                new PublicKey("GQy1uiRSpfkb3xxRXFuNhz7cCoa5P9NgEDAWyykMGB3J").equals(gatewayRegistryData.getOwner()));
        assertEquals(7, gatewayRegistryData.getCount());

        ArrayList<String> selectors = gatewayRegistryData.getSelectors();
        assertEquals(32, selectors.size());
        assertEquals("2XWUS8dNzaAFeDk6e6Q4dsojE3n9jncAZ9nNBpCJWEgZ", selectors.get(0));
        assertEquals("58no1qGYUB4FN8KKDEC2TRFRtfJeKTvXQeTeC9jhga7x", selectors.get(5));
        assertEquals("11111111111111111111111111111111", selectors.get(7));
        assertEquals("11111111111111111111111111111111", selectors.get(31));

        ArrayList<PublicKey> gateways = gatewayRegistryData.getGateways();
        assertEquals(32, gateways.size());
        assertTrue(new PublicKey("FsEACSS3nKamRKdJBaBDpZtDXWrHR2nByahr4ReoYMBH").equals(gateways.get(0)));
        assertTrue(new PublicKey("4tcoeQfSLpyd3qqnJBweTkFFqYjvn4hsv9uWP7GM94XK").equals(gateways.get(5)));
        assertTrue(new PublicKey("11111111111111111111111111111111").equals(gateways.get(7)));
        assertTrue(new PublicKey("11111111111111111111111111111111").equals(gateways.get(31)));
    }

    @Test
    public void resolveTokenGatewayContractTest() throws Exception {
        SolanaChain solanaChain = new SolanaChain(Mock.buildSolanaRpcClient(), NetworkConfig.DEVNET());

        assertTrue(new PublicKey("FsEACSS3nKamRKdJBaBDpZtDXWrHR2nByahr4ReoYMBH")
                .equals(solanaChain.resolveTokenGatewayContract()));
    }

    @Test
    public void getSPLTokenPubkeyTest() throws Exception {
        SolanaChain solanaChain = new SolanaChain(Mock.buildSolanaRpcClient(), NetworkConfig.DEVNET());

        assertTrue(
                new PublicKey("FsaLodPu4VmSwXGr3gWfwANe4vKf8XSZcCh1CEeJ3jpD").equals(solanaChain.getSPLTokenPubkey()));
    }

    @Test
    public void getAssociatedTokenAccountTest() throws Exception {
        SolanaChain solanaChain = new SolanaChain(Mock.buildSolanaRpcClient(), NetworkConfig.DEVNET());

        assertTrue(new PublicKey("4Z9Dv58aSkG9bC8stA3aqsMNXnSbJHDQTDSeddxAD1tb").equals(
                solanaChain.getAssociatedTokenAddress(new PublicKey("3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG"))));
    }

    @Test
    public void buildRenVMMessageTest() {
        byte[] pHash = Utils.fromURLBase64("xdJGAYb3IzySfn2y3McDwOUAtlPKgic7e_rYBF2FpHA");
        byte[] token = Base58.decode("2XWUS8dNzaAFeDk6e6Q4dsojE3n9jncAZ9nNBpCJWEgZ");
        String amount = "9186";
        byte[] to = Base58.decode("4Z9Dv58aSkG9bC8stA3aqsMNXnSbJHDQTDSeddxAD1tb");
        byte[] nHash = Utils.fromURLBase64("L1kPFl6zMw_k_6Vc6GZksrLeT25wROFmwbREyzlv9OQ");

        assertEquals(
                "71nK5AmnXQVYxHsA1JCF96MUuTxkgUKfXRPX97EZ5D41c8VFDyDeMunKmVp5tFbVfWNLg9S9W3Z5wKy2ZhYeMW4HJQV1tvnbizp5jM3E1wNVrvDJAcBS6xoMEMoVasZDJgvtmHtcSKNMRTJTzCf5ZBimkvdBKX9V9w81Bn8TX8apojeJQGKK3XMtAeoJWTwKqorTdewKQYwVg7iqn5xE5B1zgMy",
                Base58.encode(SolanaChain.buildRenVMMessage(pHash, amount, token, to, nHash)));
    }

    @Test
    public void findMintByDepositDetailsTest() throws Exception {
        byte[] pHash = Utils.fromURLBase64("xdJGAYb3IzySfn2y3McDwOUAtlPKgic7e_rYBF2FpHA");
        String amount = "9186";
        byte[] to = Base58.decode("4Z9Dv58aSkG9bC8stA3aqsMNXnSbJHDQTDSeddxAD1tb");
        byte[] nHash = Utils.fromURLBase64("L1kPFl6zMw_k_6Vc6GZksrLeT25wROFmwbREyzlv9OQ");

        SolanaChain solanaChain = new SolanaChain(new RpcClient(NetworkConfig.DEVNET().getEndpoint()),
                NetworkConfig.DEVNET());

        assertEquals("", solanaChain.findMintByDepositDetails(nHash, pHash, to, amount));
        ;

    }

}
