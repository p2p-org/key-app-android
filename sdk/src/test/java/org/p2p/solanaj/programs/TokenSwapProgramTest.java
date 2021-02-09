package org.p2p.solanaj.programs;

import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import static org.junit.Assert.*;
import static org.p2p.solanaj.programs.TokenSwapProgram.*;

import java.math.BigInteger;
import java.util.Base64;

import org.bitcoinj.core.Base58;


public class TokenSwapProgramTest {
    PublicKey publicKey = new PublicKey("11111111111111111111111111111111");

    @Test
    public void initializeSwapInstructionTest() {
        TransactionInstruction instruction = initializeSwapInstruction(publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, 255, 0, BigInteger.valueOf(25),
                BigInteger.valueOf(10000), BigInteger.valueOf(5), BigInteger.valueOf(10000), BigInteger.valueOf(0),
                BigInteger.valueOf(10000), BigInteger.valueOf(0), BigInteger.valueOf(0));

        assertArrayEquals(Base58.decode(
                "1WWwPPYQkGyKitVZKZnaP98twRbmDPWNxhK3L3ExxoqZAYKexQdHkniCVagZtK8nW84eUgH3ENA8DznErm72956NnASkUPL8feJGzoftV3zZr2U6msEJFfcrz1JgucZJjXLa151"),
                instruction.getData());
    }

    @Test
    public void swapInstructionTest() {
        TransactionInstruction instruction = swapInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(100000),
                BigInteger.valueOf(0));

        assertArrayEquals(Base58.decode("tSBHVn49GSCW4DNB1EYv9M"), instruction.getData());
    }

    @Test
    public void depositInstructionTest() {
        TransactionInstruction instruction = depositInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(507788),
                BigInteger.valueOf(51), BigInteger.valueOf(1038));

        assertArrayEquals(Base58.decode("22WQQtPPUknk68tx2dUGRL1Q4Vj2mkg6Hd"), instruction.getData());
    }

    @Test
    public void withdrawInstructionTest() {
        TransactionInstruction instruction = withdrawInstruction(publicKey, publicKey, publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey, publicKey, publicKey, publicKey, BigInteger.valueOf(498409),
                BigInteger.valueOf(49), BigInteger.valueOf(979));

        assertArrayEquals(Base58.decode("2aJyv2ixHWcYWoAKJkYMzSPwTrGUfnSR9R"), instruction.getData());
    }

    @Test
    public void decodeSwapData() {
        String base64Data = "Af8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAJtI5Id8QBhfDU9HbNjlM8tWzr5NFhnaIL7zaMrcQO6xAAMAAAAAAAAA6AMAAAAAAAABAAAAAAAAAOgDAAAAAAAAAAAAAAAAAAA=";

        PublicKey publicKey = new PublicKey("11111111111111111111111111111111");

        byte data[] = Base64.getDecoder().decode(base64Data);
        TokenSwapProgram.TokenSwapData tokenSwapData = TokenSwapProgram.TokenSwapData.decode(data);

        assertTrue(tokenSwapData.isInitialized());
        assertEquals(-1 /* 255 */, tokenSwapData.getNonce());

        assertEquals(publicKey.toString(), tokenSwapData.getTokenProgramId().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenAccountA().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenAccountB().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getTokenPool().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getMintA().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getMintB().toString());
        assertEquals(publicKey.toString(), tokenSwapData.getFeeAccount().toString());

        assertEquals(-101 /* 155 */, tokenSwapData.getCurveType());

        assertEquals(new BigInteger("963515510526829640"), tokenSwapData.getTradeFeeNumerator());
        assertEquals(new BigInteger("6254149569805567823"), tokenSwapData.getTradeFeeDenominator());
        assertEquals(new BigInteger("13700189867744280270"),
                new BigInteger(1, tokenSwapData.getOwnerTradeFeeNumerator().toByteArray()));
        assertEquals(new BigInteger("50083033227356403"), tokenSwapData.getOwnerTradeFeeDenominator());
        assertEquals(BigInteger.valueOf(3), tokenSwapData.getOwnerWithdrawFeeNumerator());
        assertEquals(BigInteger.valueOf(1000), tokenSwapData.getOwnerWithdrawFeeDenominator());
        assertEquals(BigInteger.valueOf(1), tokenSwapData.getHostFeeNumerator());
        assertEquals(BigInteger.valueOf(1000), tokenSwapData.getHostFeeDenominator());
        //assertEquals(BigInteger.valueOf(0), tokenSwapData.getAmp());

    }

}
