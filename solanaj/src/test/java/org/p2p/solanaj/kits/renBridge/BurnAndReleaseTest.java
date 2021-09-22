package org.p2p.solanaj.kits.renBridge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class BurnAndReleaseTest {

    @Test
    public void getBurnStateTest() throws Exception {
        BurnAndRelease burnAndRelease = new BurnAndRelease(NetworkConfig.DEVNET());
        SolanaChain.BurnDetails burnDetails = new SolanaChain.BurnDetails();
        burnDetails.confirmedSignature = "2kNe8duPRcE9xxKLLVP92e9TBH5WvmVVWQJ18gEjqhgxsrKtBEBVfeXNFz5Un3yEEQJZkxY2ysQR4dGQaytnDM1i";
        burnDetails.nonce = BigInteger.valueOf(35);
        burnDetails.recepient = "tb1ql7w62elx9ucw4pj5lgw4l028hmuw80sndtntxt";
        assertEquals("I_HJMksqVC5_-0G9FE_z8AORRDMoxl1vZbSGEc2VfJ4",
                burnAndRelease.getBurnState(burnDetails, "1000").txHash);
    }

}
