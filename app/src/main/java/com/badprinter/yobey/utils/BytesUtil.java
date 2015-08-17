package com.badprinter.yobey.utils;

/**
 * Created by root on 15-8-17.
 */
public class BytesUtil {
    public static String bytesToHex(byte[] bytes) {
        String str = "";
        if (bytes == null || bytes.length <= 0) {
            return str;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                str = str + "%0" + hv;
            } else {
                str = str + "%" + hv;
            }
        }
        return str;
    }
}
