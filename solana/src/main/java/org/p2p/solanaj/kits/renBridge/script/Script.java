package org.p2p.solanaj.kits.renBridge.script;

import org.p2p.solanaj.utils.Hash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Script {
    private static final int CHECKSUM_LENGTH = 4;
    private ByteArrayOutputStream bos;

    public Script() {
        bos = new ByteArrayOutputStream();
    }

    public Script op(int opcode) {
        bos.write(opcode);
        return this;
    }

    public Script data(byte[] data) throws IOException {
        bos.write(data.length);
        bos.write(data);
        return this;
    }

    public byte[] toByteArray() {
        return bos.toByteArray();
    }

    public byte[] toAddress(byte[] prefix) {
        byte[] hash = Hash.hash160(this.toByteArray());
        byte[] hashWithPrefix = ByteBuffer.allocate(prefix.length + hash.length).put(prefix).put(hash).array();
        byte[] hashWithChecksum = ByteBuffer.allocate(hashWithPrefix.length + CHECKSUM_LENGTH).put(hashWithPrefix)
                .put(checksum(hashWithPrefix)).array();
        return hashWithChecksum;
    }

    public static Script gatewayScript(byte[] gGubKeyHash, byte[] gHash) throws IOException {
        Script script = new Script();
        script.data(gHash);
        script.op(ScriptOpCodes.OP_DROP);
        script.op(ScriptOpCodes.OP_DUP);
        script.op(ScriptOpCodes.OP_HASH160);
        script.data(gGubKeyHash);
        script.op(ScriptOpCodes.OP_EQUALVERIFY);
        script.op(ScriptOpCodes.OP_CHECKSIG);
        return script;
    }

    public static byte[] createAddressByteArray(byte[] gGubKeyHash, byte[] gHash, byte[] prefix) throws IOException {
        return gatewayScript(gGubKeyHash, gHash).toAddress(prefix);
    }

    public static byte[] checksum(byte[] hash) {
        byte[] sha256sha256Hash = Hash.sha256(Hash.sha256(hash));
        return Arrays.copyOf(sha256sha256Hash, CHECKSUM_LENGTH);
    }

    public static class ScriptOpCodes {
        public static final int OP_DROP = 0x75;
        public static final int OP_DUP = 0x76;
        public static final int OP_HASH160 = 0xa9;
        public static final int OP_EQUALVERIFY = 0x88;
        public static final int OP_CHECKSIG = 0xac;
    }

}
