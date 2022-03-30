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
        assertEquals(1630627200000L, session.getExpiryTime());
        assertEquals("2020202020202020202020202020202020202020202020202020202034396236", session.getNonce());
    }
}
