package org.p2p.solanaj.utils;

import org.bitcoinj.core.Bech32;
import org.p2p.core.utils.Base58Utils;
import org.p2p.core.crypto.Base64UrlUtils;
import org.p2p.core.crypto.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils {
    public static long getSessionDay() {
        return (long) Math.floor(System.currentTimeMillis() / 1000 / 60 / 60 / 24);
    }

    public static long getSessionExpiry() {
        return getSessionExpiry(getSessionDay());
    }

    public static long getSessionExpiry(long sessionDay) {
        return (sessionDay + 3) * 60 * 60 * 24 * 1000;
    }

    public static String generateNonce() {
        return generateNonce(getSessionDay());
    }

    public static String generateNonce(long sessionDay) {
        char arr[] = new char[28];
        Arrays.fill(arr, " ".charAt(0));
        byte[] nonceBytes = String.copyValueOf(arr).concat(Long.toHexString(sessionDay)).getBytes();
        return Hex.INSTANCE.encode(nonceBytes);
    }

    public static String toURLBase64(String hexSrc) {
        return toURLBase64(Hex.INSTANCE.decode(hexSrc));
    }

    public static String toURLBase64(byte[] src) {
        return Base64UrlUtils.toURLBase64(src).replace("=", "");
    }

    public static byte[] fromURLBase64(String src) {
        return Base64UrlUtils.fromURLBase64(src);
    }

    public static byte[] amountToUint256ByteArrayBE(String amount) {
        byte[] amountBytes = new BigInteger(amount).toByteArray();
        ByteBuffer amountBuffer = ByteBuffer.allocate(32);
        amountBuffer.position(32 - amountBytes.length);
        amountBuffer.put(amountBytes);
        return amountBuffer.array();
    }

    public static String reverseHex(String src) {
        char[] arr = src.toCharArray();
        char tmp;
        for (int i = 0; i < arr.length / 2; i += 2) {
            tmp = arr[i];
            arr[i] = arr[arr.length - i - 2];
            arr[arr.length - i - 2] = tmp;

            tmp = arr[i + 1];
            arr[i + 1] = arr[arr.length - i - 1];
            arr[arr.length - i - 1] = tmp;
        }
        return new String(arr);
    }

    static String secp256k1nHEX = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141".toLowerCase();
    static BigInteger secp256k1n = new BigInteger(Hex.INSTANCE.decode(secp256k1nHEX));

    public static byte[] fixSignatureSimple(String sig) {
        byte[] sigBytes = fromURLBase64(sig);
        byte[] r = Arrays.copyOfRange(sigBytes, 0, 32);
        byte[] s = Arrays.copyOfRange(sigBytes, 32, 64);
        double v = sigBytes[64] % 27;

        BigInteger sBN = new BigInteger(s);
        double vFixed = (v % 27) + 27;
        // FIXME
        if (sBN.compareTo(secp256k1n.divide(BigInteger.valueOf(2))) != 1) {
            sBN = secp256k1n.subtract(sBN);
            vFixed = v == 27 ? 28 : 27;
        }

        byte[] sBNBytes = sBN.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(r.length + sBNBytes.length + 1);
        buffer.put(r).put(sBNBytes).put((byte) vFixed);
        return buffer.array();
    }

    public static byte[] uint64ToByteArrayLE(BigInteger val) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteUtils.uint64ToByteStreamLE(val, bos);
        return bos.toByteArray();
    }

    public static byte[] addressToBytes(String address) {
        try {
            // For bitcoin BECH32 address types
            Bech32.Bech32Data data = Bech32.decode(address);
            byte type = data.data[0];
            byte[] words = Arrays.copyOfRange(data.data, 1, data.data.length);
            byte[] fromWords = fromWords(words);
            ByteBuffer buffer = ByteBuffer.allocate(1 + fromWords.length);
            buffer.put(type);
            buffer.put(fromWords(words));
            return buffer.array();
        } catch (Throwable e) {
            // For legacy bitcoin P2PKH or P2SH address types
            return Base58Utils.INSTANCE.decode(address);
        }
    }

    private static byte[] convert(byte[] data, int inBits, int outBits, boolean pad) throws Exception {
        int value = 0;
        int bits = 0;
        int maxV = (1 << outBits) - 1;

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (int i = 0; i < data.length; ++i) {
            value = (value << inBits) | data[i];
            bits += inBits;

            while (bits >= outBits) {
                bits -= outBits;
                result.write((value >> bits) & maxV);
            }
        }

        if (pad) {
            if (bits > 0) {
                result.write((value << (outBits - bits)) & maxV);
            }
        } else {
            if (bits >= inBits) throw new Exception("Excess padding");
        }

        return result.toByteArray();
    }

    public static byte[] fromWords(byte[] words) throws Exception {
        return convert(words, 5, 8, false);
    }

}