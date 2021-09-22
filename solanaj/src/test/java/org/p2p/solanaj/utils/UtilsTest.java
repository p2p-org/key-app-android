package org.p2p.solanaj.utils;

import org.bitcoinj.core.Base58;
import org.junit.Test;

import static org.bitcoinj.core.Utils.HEX;
import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void fixSignatureSimpleTest() {
        assertEquals("CDsK2CsmBnLqupzsv9EeDHwc5ZYQxXt9LKzpkmusasc5z2LdDiKHqnCXpiCZTEXDYZtP7JgY4Ur9fkAU5RWSwxrnn",
                Base58.encode(Utils.fixSignatureSimple(
                        "fypvW39VUS6tB8basjmi3YsSn_GR7uLTw_lGcJhQYFcRVemsA1LkF8FQKH_1XJR-bQGP6AXsPbnmB1H8AvKBWgA")));
    }

    @Test
    public void addressToBytesTest() throws Exception {
        assertEquals("0x00ff9da567e62f30ea8654fa1d5fbd47bef8e3be13",
                "0x".concat(HEX.encode(Utils.addressToBytes("tb1ql7w62elx9ucw4pj5lgw4l028hmuw80sndtntxt"))));
    }

}
