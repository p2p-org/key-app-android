package org.p2p.core.crypto;

import android.annotation.SuppressLint;

import java.util.Base64;

@SuppressLint("NewApi")
public class Base64UrlUtils {

    public static String toURLBase64(byte[] src) {
        return Base64.getUrlEncoder().encodeToString(src).replace("=", "");
    }

    public static byte[] fromURLBase64(String src) {
        return Base64.getUrlDecoder().decode(src);
    }
}
