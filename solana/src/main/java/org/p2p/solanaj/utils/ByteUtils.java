package org.p2p.solanaj.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import static org.bitcoinj.core.Utils.reverseBytes;

public class ByteUtils {
    public static final int UINT_16_LENGTH = 2;
    public static final int UINT_32_LENGTH = 4;
    public static final int UINT_64_LENGTH = 8;
    public static final int UINT_128_LENGTH = 16;

    public static byte[] readBytes(byte[] buf, int offset, int length) {
        byte[] b = new byte[length];
        System.arraycopy(buf, offset, b, 0, length);
        return b;
    }

    public static BigInteger readUint64(byte[] buf, int offset) {
        return new BigInteger(reverseBytes(readBytes(buf, offset, UINT_64_LENGTH)));
    }

    public static BigInteger readUint128(byte[] buf, int offset) {
        return new BigInteger(reverseBytes(readBytes(buf, offset, UINT_128_LENGTH)));
    }

    public static void uint64ToByteStreamLE(BigInteger val, OutputStream stream) throws IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) {
            if (bytes[0] == 0) {
                bytes = readBytes(bytes, 1, bytes.length - 1);
            } else {
                throw new RuntimeException("Input too large to encode into a uint64");
            }
        }
        bytes = reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                stream.write(0);
        }
    }

    public static byte[] uint16ToByteArrayLE(int val) {
        byte[] out = new byte[UINT_16_LENGTH];
        out[0] = (byte) (0xFF & val);
        out[1] = (byte) (0xFF & (val >> 8));
        return out;
    }

    public static byte[] uint32ToByteArrayBE(long val) {
        byte[] out = new byte[UINT_32_LENGTH];
        out[0] = (byte) (0xFF & (val >> 24));
        out[1] = (byte) (0xFF & (val >> 16));
        out[2] = (byte) (0xFF & (val >> 8));
        out[3] = (byte) (0xFF & val);
        return out;
    }

    public static int readUint16(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }
}
