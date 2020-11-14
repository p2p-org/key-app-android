package com.p2p.wowlet.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Transfer {
    public byte[] compile() {
        ByteBuffer result = ByteBuffer.allocate(17); // FIXME

        result.put((byte) 0x2); // programIndex
        byte[] keyIndeces = new byte[]{0, 1};

        short[] keysSize = ShortvecEncoding.encodeLength(keyIndeces.length);

        result.put((byte) keysSize[0]); // FIXME
        result.put(keyIndeces);

        result.put((byte) 12); // FIXME transfer data size
        result.order(ByteOrder.LITTLE_ENDIAN).putInt(2); // instruction index

        int lamports = 1550;
        result.order(ByteOrder.LITTLE_ENDIAN).putInt(lamports);

        return result.array();
    }
}
