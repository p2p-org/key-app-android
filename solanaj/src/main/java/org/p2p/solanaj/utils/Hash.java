package org.p2p.solanaj.utils;

import org.bitcoinj.core.Sha256Hash;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;

import static org.bitcoinj.core.Utils.sha256hash160;

public class Hash {

    public static byte[] sha256(byte[] input) {
        return Sha256Hash.hash(input);
    }

    public static byte[] hash160(byte[] input) {
        return sha256hash160(input);
    }

    public static byte[] keccak256(byte[] input) {
        KeccakDigest digest = new KeccakDigest(256);
        digest.update(input, 0, input.length);
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

    public static byte[] generatePHash() {
        return keccak256(new byte[]{});
    }

    public static byte[] generateSHash() {
        return generateSHash("BTC/toSolana");
    }

    public static byte[] generateSHash(String selector) {
        return keccak256(selector.getBytes());
    }

    public static byte[] generateGHash(String to, String tokenIdentifier, byte[] nonce) {
        byte[] pHash = generatePHash();
        byte[] sHash = Hex.decode(tokenIdentifier);
        byte[] toBytes = Hex.decode(to);

        ByteBuffer buffer = ByteBuffer.allocate(pHash.length + sHash.length + toBytes.length + nonce.length);
        buffer.put(pHash).put(sHash).put(toBytes).put(nonce);

        return keccak256(buffer.array());
    }

    public static byte[] generateNHash(byte[] nonce, byte[] txId, String txIndex) {
        ByteBuffer buffer = ByteBuffer.allocate(nonce.length + txId.length + ByteUtils.UINT_32_LENGTH);
        buffer.put(nonce).put(txId).put(ByteUtils.uint32ToByteArrayBE(Long.valueOf(txIndex)));
        return keccak256(buffer.array());
    }

}