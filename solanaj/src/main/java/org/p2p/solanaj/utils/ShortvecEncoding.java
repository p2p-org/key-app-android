package org.p2p.solanaj.utils;

import static org.bitcoinj.core.Utils.uint16ToByteArrayLE;

public class ShortvecEncoding {

    public static byte[] encodeLength(int len) {
        byte[] out = new byte[10];
        int remLen = len;
        int cursor = 0;

        for (; ; ) {
            int elem = remLen & 0x7f;
            remLen >>= 7;
            if (remLen == 0) {
                uint16ToByteArrayLE(elem, out, cursor);
                break;
            } else {
                elem |= 0x80;
                uint16ToByteArrayLE(elem, out, cursor);
                cursor += 1;
            }
        }

        byte[] bytes = new byte[cursor + 1];
        System.arraycopy(out, 0, bytes, 0, cursor + 1);

        return bytes;
    }

    public static int decodeLength(byte[] data) {
        byte len = 0;
        byte size = 0;

        int cursor = 0;

        while (true) {
            if (data.length - cursor == 0) break;

            int element = data[cursor++];

            len |= (element & 0x7F) << (size * 7);
            size += 1;

            if ((element & 0x80) == 0) {
                break;
            }
        }

        return len;
    }
}
