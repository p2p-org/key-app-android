package org.p2p.solanaj.utils.crypto;

import android.os.Build;

import java.util.Base64;

public class Base64UrlUtils {

    public static String toURLBase64(byte[] src) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlEncoder().encodeToString(src).replace("=", "");
        } else {
            return Base64Utils.INSTANCE.encode(src);
        }
    }

    public static byte[] fromURLBase64(String src) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getUrlDecoder().decode(src);
        } else {
            return Base64Utils.INSTANCE.decode(src);
        }
    }
}
