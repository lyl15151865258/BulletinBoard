package com.zhongbenshuo.bulletinboard.utils.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-512加密，不可逆
 * Created by Song on 2017/2/22.
 */

public class SHAUtils {

    /**
     * SHA-1","SHA-256","SHA-384","MD5" 加密
     *
     * @param strSrc  需要加密的字符串
     * @param encName "SHA-1","SHA-256","SHA-384","MD5"
     * @return 加密后的字符串
     */
    public static String sign(String strSrc, String encName) {
        MessageDigest md;
        String strDes;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "签名失败," + e.getMessage();
        }
        return strDes;
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuilder des = new StringBuilder();
        String tmp;
        for (byte bt : bts) {
            tmp = (Integer.toHexString(bt & 0xFF));
            if (tmp.length() == 1) {
                des.append("0");
            }
            des.append(tmp);
        }
        return des.toString();
    }
}
