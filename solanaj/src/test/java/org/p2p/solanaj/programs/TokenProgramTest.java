package org.p2p.solanaj.programs;

import org.bitcoinj.core.Base58;
import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.math.BigInteger;
import java.util.Base64;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.p2p.solanaj.programs.TokenProgram.approveInstruction;
import static org.p2p.solanaj.programs.TokenProgram.closeAccountInstruction;
import static org.p2p.solanaj.programs.TokenProgram.createAssociatedTokenAccountInstruction;
import static org.p2p.solanaj.programs.TokenProgram.createTransferCheckedInstruction;
import static org.p2p.solanaj.programs.TokenProgram.initializeAccountInstruction;
import static org.p2p.solanaj.programs.TokenProgram.initializeMintInstruction;
import static org.p2p.solanaj.programs.TokenProgram.mintToInstruction;
import static org.p2p.solanaj.programs.TokenProgram.transferInstruction;

public class TokenProgramTest {
    PublicKey publicKey = new PublicKey("11111111111111111111111111111111");

    @Test
    public void initializeMint() {
        TransactionInstruction instruction = initializeMintInstruction(publicKey, publicKey, 2, publicKey, null);

        assertArrayEquals(
                Base58.decode(
                        "1nBYrAcY5rLBEoZaGN6hahh3ZhgQTgLmsmunRee8a3zaA42wLZ7aux2HiX6tCKp73MWVj6fDagGuhfTPYGY32UNcis"),
                instruction.getData());
    }

    @Test
    public void initializeAccount() {
        TransactionInstruction instruction = initializeAccountInstruction(publicKey, publicKey, publicKey, publicKey);

        assertArrayEquals(Base58.decode("2"), instruction.getData());
    }

    @Test
    public void transfer() {
        TransactionInstruction instruction = transferInstruction(publicKey, publicKey, publicKey, publicKey,
                BigInteger.valueOf(100));

        assertArrayEquals(Base58.decode("3WBgs5fm8oDy"), instruction.getData());
    }

    @Test
    public void approve() {
        TransactionInstruction instruction = approveInstruction(publicKey, publicKey, publicKey, publicKey,
                new BigInteger("1000"));

        assertArrayEquals(Base58.decode("4d5tSvUuzUVM"), instruction.getData());
    }

    @Test
    public void mintTo() {
        TransactionInstruction instruction = mintToInstruction(publicKey, publicKey, publicKey, publicKey,
                new BigInteger("1000000000"));

        assertArrayEquals(Base58.decode("6AsKhot84V8s"), instruction.getData());
    }

    @Test
    public void closeAccount() {
        TransactionInstruction instruction = closeAccountInstruction(publicKey, publicKey, publicKey, publicKey);

        assertArrayEquals(Base58.decode("A"), instruction.getData());
    }

    @Test
    public void createTransferChecked() {
        TransactionInstruction instruction = createTransferCheckedInstruction(publicKey, publicKey, publicKey,
                publicKey, publicKey, BigInteger.valueOf(1000), 6);

        assertArrayEquals(Base58.decode("j4EYRhtmbmRQq"), instruction.getData());
    }

    @Test
    public void createAssociatedTokenAccount() {
        TransactionInstruction instruction = createAssociatedTokenAccountInstruction(publicKey, publicKey, publicKey,
                publicKey, publicKey, publicKey);

        assertEquals(0, instruction.getData().length);
        assertEquals(7, instruction.getKeys().size());
    }

    @Test
    public void decodeMintData() {
        TokenProgram.MintData mintData = TokenProgram.MintData.decode(Base64.getDecoder().decode(
                "AQAAAAYa2dBThxVIU37ePiYYSaPft/0C+rx1siPI5GrbhT0MABCl1OgAAAAGAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="));

        assertEquals("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo", mintData.getMintAuthority().toString());
        assertEquals(new BigInteger("1000000000000"), mintData.getSupply());
        assertEquals(6, mintData.getDecimals());
        assertTrue(mintData.isInitialized());
        assertNull(mintData.getFreezeAuthority());
    }

    @Test
    public void decodeAccountInfoData() {
        TokenProgram.AccountInfoData accountInfoData = TokenProgram.AccountInfoData.decode(Base64.getDecoder().decode(
                "BhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQwCqmOzhzy1ve5l2AqL0ottCChJZ1XSIW3k3C7TaBQn7aCGAQAAAAAAAQAAAOt6vNDYdevCbaGxgaMzmz7yoxaVu3q9vGeCc7ytzeWqAQAAAAAAAAAAAAAAAGQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));

        assertEquals("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo", accountInfoData.getMint().toString());
        assertEquals("BQWWFhzBdw2vKKBUX17NHeFbCoFQHfRARpdztPE2tDJ", accountInfoData.getOwner().toString());
        assertEquals(BigInteger.valueOf(100000), accountInfoData.getAmount());
        assertEquals("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5", accountInfoData.getDelegate().toString());
        assertEquals(BigInteger.valueOf(100), accountInfoData.getDelegatedAmount());
        assertFalse(accountInfoData.isNative());
        assertTrue(accountInfoData.isInitialized());
        assertFalse(accountInfoData.isFrozen());
        assertNull(accountInfoData.getRentExemptReserve());
        assertNull(accountInfoData.getCloseAuthority());
    }

    @Test
    public void decodeAccountInfoData1() {
        TokenProgram.AccountInfoData accountInfoData = TokenProgram.AccountInfoData.decode(Base64.getDecoder().decode(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAOt6vNDYdevCbaGxgaMzmz7yoxaVu3q9vGeCc7ytzeWq"));

        assertNull(accountInfoData.getDelegate());
        assertEquals(BigInteger.valueOf(0), accountInfoData.getDelegatedAmount());
        assertFalse(accountInfoData.isInitialized());
        assertFalse(accountInfoData.isNative());
        assertNull(accountInfoData.getRentExemptReserve());
        assertEquals("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5", accountInfoData.getCloseAuthority().toString());

        accountInfoData = TokenProgram.AccountInfoData.decode(Base64.getDecoder().decode(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAOt6vNDYdevCbaGxgaMzmz7yoxaVu3q9vGeCc7ytzeWq"));
        assertTrue(accountInfoData.isFrozen());
    }

}
