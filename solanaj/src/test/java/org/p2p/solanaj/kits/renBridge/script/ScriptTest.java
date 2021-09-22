package org.p2p.solanaj.kits.renBridge.script;

import org.bitcoinj.core.Base58;
import org.junit.Test;

import java.io.IOException;

import static org.bitcoinj.core.Utils.sha256hash160;
import static org.junit.Assert.assertEquals;

public class ScriptTest {

    @Test
    public void gatewayScriptTest() {
        byte[] gGubKeyHash = Base58.decode("3ou4DtLwVsvkX76Ay3q5H4ccKQdw");
        byte[] gHash = Base58.decode("2zB96eXCpNt4oHXqxeHjRyphdWaGYz1attyyjcSoqpV1");

        try {
            assertEquals("2HnZgcJKmdCVaP9mMdzHM1gsEhbDfusQLZAupRU6AnZnD4yFsHKoUCzb3JWTmKM6PscSpRSbbLAF4y4fu",
                    Base58.encode(Script.gatewayScript(gGubKeyHash, gHash).toByteArray()));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void createAddressByteArrayTest() {
        byte[] gGubKeyHash = Base58.decode("ucy1GEq7vwmysYhuGMedLFDUXUQchvfKAQLxEopvzU9h");
        byte[] gHash = Base58.decode("2zB96eXCpNt4oHXqxeHjRyphdWaGYz1attyyjcSoqpV1");
        byte[] prefix = new byte[]{(byte) 0xc4};

        try {
            assertEquals("2MuoKWBjtBEhxgrE6bvY9TGjkGUTPeEq1Ja",
                    Base58.encode(Script.createAddressByteArray(sha256hash160(gGubKeyHash), gHash, prefix)));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Test
    public void checksumTest() {
        byte[] hash = Base58.decode("D4Rioa1Zh1jMummwpqx8m2SkhSATN");

        assertEquals("2vuP8n", Base58.encode(Script.checksum(hash)));
    }

}
