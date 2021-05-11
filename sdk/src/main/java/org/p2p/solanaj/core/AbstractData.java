package org.p2p.solanaj.core;

import java.math.BigInteger;
import org.bitcoinj.core.Utils;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.utils.ByteUtils;

public abstract class AbstractData {
    private transient byte[] data;
    private transient int cursor = 0;

    protected AbstractData(byte[] data, int dataLength) {
        if (data.length < dataLength) {
            throw new IllegalArgumentException("wrong data");
        }

        this.data = data;
    }

    protected byte readByte() {
        return data[cursor++];
    }

    protected PublicKey readPublicKey() {
        PublicKey pk = PublicKey.readPubkey(data, cursor);
        cursor += PublicKey.PUBLIC_KEY_LENGTH;
        return pk;
    }

    protected long readUint32() {
        long value = Utils.readUint32(data, cursor);
        cursor += ByteUtils.UINT_32_LENGTH;
        return value;
    }

    protected BigInteger readUint64() {
        BigInteger uint64 = ByteUtils.readUint64(data, cursor);
        cursor += ByteUtils.UINT_64_LENGTH;
        return uint64;
    }

}
