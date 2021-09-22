package org.p2p.solanaj.kits.renBridge.renVM;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.p2p.solanaj.kits.renBridge.renVM.RenVMProvider;
import org.p2p.solanaj.kits.renBridge.renVM.types.ParamsSubmitMint;
import org.p2p.solanaj.utils.Utils;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

public class RenVMProviderTest {

    @Test
    public void hashTransactionTest() {
        ParamsSubmitMint.MintTransactionInput mintTx = new ParamsSubmitMint.MintTransactionInput();
        mintTx.txid = "tNWySkdaqjoHEJddH3jlVTwLFOJikwjxlGNiLDXC2ns";
        mintTx.txindex = "1";
        mintTx.ghash = "ePjNFLH84OxeVjzihYVWVbFZhyFM0ZpegupiBUt76V8";
        mintTx.gpubkey = "Aw3WX32ykguyKZEuP0IT3RUOX5csm3PpvnFNhEVhrDVc";
        mintTx.nhash = "_jRsczCRyXm_Wud_oLxiHQpUTyf0q3iUy7FBpR-m5VQ";
        mintTx.nonce = "ICAgICAgICAgICAgICAgICAgICAgICAgICAgIDQ5Yjg";
        mintTx.phash = "xdJGAYb3IzySfn2y3McDwOUAtlPKgic7e_rYBF2FpHA";
        mintTx.to = "4Z9Dv58aSkG9bC8stA3aqsMNXnSbJHDQTDSeddxAD1tb";
        mintTx.amount = "10000";

        assertEquals("3eT3xmt8h9wW9OZVvfV-BQo5nm70c_ClEqe4zryBq54",
                Utils.toURLBase64(RenVMProvider.hashTransactionMint(mintTx, "BTC/toSolana")));
    }

}
