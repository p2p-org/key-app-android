package org.p2p.solanaj.crypto;

/**
 * Defines a key with a given private key
 */
public class HdPrivateKey extends HdKey {
    private byte[] privateKey;

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
}