package org.p2p.solanaj.kits.renBridge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.Utils;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class LockAndMintTest {
    private PublicKey destinationAddress = new PublicKey("3h1zGmCwsRJnVk5BuRNMLsPaQu1y2aqXqXDWYCgrp5UG");
    private long sessionDay = 18870;
    private LockAndMint.Session session = new LockAndMint.Session(
            destinationAddress,
            Utils.generateNonce(sessionDay),
            sessionDay,
            Utils.getSessionExpiry(sessionDay),
            null
    );

    @Test
    public void sessionTest() {
        assertEquals(1630627200000L, session.expiryTime);
        assertEquals("2020202020202020202020202020202020202020202020202020202034396236", session.nonce);
    }

    @Test
    public void generateGatewayAddressTest() throws Exception {
        LockAndMint lockAndMint = new LockAndMint(NetworkConfig.DEVNET(), Mock.buildRenVMProvider(),
                new SolanaChain(Mock.buildSolanaRpcClient(), NetworkConfig.DEVNET()), session);

        assertEquals("2NC451uvR7AD5hvWNLQiYoqwQQfvQy2XB6U", lockAndMint.generateGatewayAddress());
    }

    @Test
    public void getDepositStateTest() throws Exception {
        LockAndMint lockAndMint = new LockAndMint(NetworkConfig.DEVNET(), Mock.buildRenVMProvider(),
                new SolanaChain(Mock.buildSolanaRpcClient(), NetworkConfig.DEVNET()),
                new LockAndMint.Session(destinationAddress, Utils.generateNonce(18874), 18874, Utils.getSessionExpiry(18874), null));
        String gatewayAddress = lockAndMint.generateGatewayAddress();

        assertEquals("2MyJ7zQxBCnwKuRNoE3UYD2cb9MDjdkacaF", gatewayAddress);
        assertEquals("LLg3jxVXS4NEixjaBOUXocRqaK_Y0wk5HPshI1H3e6c",
                lockAndMint.getDepositState("01d32c22d721d7bf0cd944fc6e089b01f998e1e77db817373f2ee65e40e9462a", "0",
                        "10000").txHash);
    }

}
