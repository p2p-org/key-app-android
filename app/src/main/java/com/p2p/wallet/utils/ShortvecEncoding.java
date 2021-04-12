package com.p2p.wallet.utils;

public class ShortvecEncoding {
    public static short[]

    encodeLength(int len) {
        short[] bytes = new short[5]; // FIXME
        int rem_len = len;
        int position = 0;

        for (;;)
        {
            int elem = rem_len & 0x7f;
            rem_len >>= 7;
            if (rem_len == 0) {
                bytes[position] = (short) elem;
                break;
            } else {
                elem |= 0x80;
                bytes[position] = (short) elem;
                position += 1;
            }
        }

        short[] result = new short[position + 1];
        System.arraycopy(bytes, 0, result, 0, position + 1);

        return result;
    }
}
