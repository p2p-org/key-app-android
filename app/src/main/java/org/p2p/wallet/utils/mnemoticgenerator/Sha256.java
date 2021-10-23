package org.p2p.wallet.utils.mnemoticgenerator;

import java.security.MessageDigest;

import static org.p2p.wallet.utils.mnemoticgenerator.CheckedExceptionToRuntime.toRuntime;

public final class Sha256 {

    Sha256() {
    }

    public static byte[] sha256(final byte[] bytes) {
        return sha256(bytes, 0, bytes.length);
    }

    public static byte[] sha256(final byte[] bytes, final int offset, final int length) {
        final MessageDigest digest = sha256();
        digest.update(bytes, offset, length);
        return digest.digest();
    }

    public static byte[] sha256Twice(final byte[] bytes) {
        return sha256Twice(bytes, 0, bytes.length);
    }

    public static byte[] sha256Twice(final byte[] bytes, final int offset, final int length) {
        final MessageDigest digest = sha256();
        digest.update(bytes, offset, length);
        digest.update(digest.digest());
        return digest.digest();
    }

    private static MessageDigest sha256() {
        return toRuntime(() -> MessageDigest.getInstance("SHA-256"));
    }
}
