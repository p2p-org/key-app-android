package org.p2p.solanaj.programs;

import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.math.BigInteger;
import java.util.Base64;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TokenSwapProgramTest {
    PublicKey publicKey = new PublicKey("11111111111111111111111111111111");

    @Test
    public void initializeSwapInstructionTest() {
        TransactionInstruction instruction = TokenSwapProgram.initializeSwapInstruction(publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, 255, 0, BigInteger.valueOf(25),
                BigInteger.valueOf(10000), BigInteger.valueOf(5), BigInteger.valueOf(10000), BigInteger.valueOf(0),
                BigInteger.valueOf(10000), BigInteger.valueOf(0), BigInteger.valueOf(0));

        assertArrayEquals(Base58.decode(
                "1WWwPPYQkGyKitVZKZnaP98twRbmDPWNxhK3L3ExxoqZAYKexQdHkniCVagZtK8nW84eUgH3ENA8DznErm72956NnASkUPL8feJGzoftV3zZr2U6msEJFfcrz1JgucZJjXLa151"),
                instruction.getData());
    }

    @Test
    public void swapInstructionTest() {
        TransactionInstruction instruction = TokenSwapProgram.swapInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(100000),
                BigInteger.valueOf(0));

        assertArrayEquals(Base58.decode("tSBHVn49GSCW4DNB1EYv9M"), instruction.getData());
    }

    @Test
    public void depositInstructionTest() {
        TransactionInstruction instruction = TokenSwapProgram.depositInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(507788),
                BigInteger.valueOf(51), BigInteger.valueOf(1038));

        assertArrayEquals(Base58.decode("22WQQtPPUknk68tx2dUGRL1Q4Vj2mkg6Hd"), instruction.getData());
    }

    @Test
    public void withdrawInstructionTest() {
        TransactionInstruction instruction = TokenSwapProgram.withdrawInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(498409),
                BigInteger.valueOf(49), BigInteger.valueOf(979));

        assertArrayEquals(Base58.decode("2aJyv2ixHWcYWoAKJkYMzSPwTrGUfnSR9R"), instruction.getData());
    }

    @Test
    public void decodeSwapData() {
        String base64Data = "AQH8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAeAAAAAAAAABAnAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        PublicKey publicKey = new PublicKey("11111111111111111111111111111111");

        byte data[] = Base64.getDecoder().decode(base64Data);
        TokenSwapProgram.TokenSwapData tokenSwapData = TokenSwapProgram.TokenSwapData.decode(data);

        assertEquals(1, tokenSwapData.getVersion());
        assertTrue(tokenSwapData.isInitialized());
        assertEquals(-4, tokenSwapData.getNonce());

        assertEquals(publicKey.toString(), tokenSwapData.getTokenProgramId().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenAccountA().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenAccountB().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenPool().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getMintA().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getMintB().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getFeeAccount().toString());

        assertEquals(BigInteger.valueOf(30), tokenSwapData.getTradeFeeNumerator());
        assertEquals(BigInteger.valueOf(10000), tokenSwapData.getTradeFeeDenominator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getOwnerTradeFeeNumerator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getOwnerTradeFeeDenominator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getOwnerWithdrawFeeNumerator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getOwnerWithdrawFeeDenominator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getHostFeeNumerator());
        assertEquals(BigInteger.valueOf(0), tokenSwapData.getHostFeeDenominator());

        assertEquals(0, tokenSwapData.getCurveType());
    }

}
