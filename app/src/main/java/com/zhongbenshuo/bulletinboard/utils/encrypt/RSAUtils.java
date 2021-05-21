package com.zhongbenshuo.bulletinboard.utils.encrypt;

import android.util.Base64;
import android.util.SparseArray;

import com.zhongbenshuo.bulletinboard.utils.LogUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * RSA: 既能用于数据加密也能用于数字签名的算法
 * RSA算法原理如下：
 * 1.随机选择两个大质数p和q，p不等于q，计算N=pq
 * 2.选择一个大于1小于N的自然数e，e必须与(p-1)(q-1)互素
 * 3.用公式计算出d：d×e = 1 (mod (p-1)(q-1))
 * 4.销毁p和q
 * 5.最终得到的N和e就是“公钥”，d就是“私钥”，发送方使用N去加密数据，接收方只有使用d才能解开数据内容
 * <p>
 * 基于大数计算，比DES要慢上几倍，通常只能用于加密少量数据或者加密密钥
 * 私钥加解密都很耗时，服务器要求解密效率高，客户端私钥加密，服务器公钥解密比较好一点
 */

public class RSAUtils {

    private static final String TAG = "RSAUtils";

    // 用于封装随机产生的公钥与私钥
    public static SparseArray<String> keyMap = new SparseArray<>();

    // 服务端必须保持一致，否则无法解密
    private static final String ALGORITHM = "RSA/None/PKCS1Padding";
    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 随机生成密钥对
     *
     * @param length 密钥长度
     */
    public static SparseArray<String> genKeyPair(int length) throws Exception {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(length, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = base64Encode(publicKey.getEncoded());
        String privateKeyString = base64Encode(privateKey.getEncoded());
        LogUtils.d(TAG, "公钥：" + publicKeyString);
        LogUtils.d(TAG, "私钥：" + privateKeyString);
        // 将公钥和私钥保存到Map
        keyMap.put(0, publicKeyString);   //0表示公钥
        keyMap.put(1, privateKeyString);  //1表示私钥
        return keyMap;
    }

    /**
     * RSA公钥加密
     *
     * @param str       待加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encryptByPublicKey(String str, String publicKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = base64Decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return base64Encode(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * RSA私钥加密
     *
     * @param str        待加密字符串
     * @param privateKey 私钥
     * @return 密文
     */
    public static String encryptByPrivateKey(String str, String privateKey) throws Exception {
        //base64编码的公钥
        byte[] decoded = base64Decode(privateKey);
        RSAPrivateKey pubKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return base64Encode(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 加密，返回BASE64编码的字符串
     *
     * @param key 公钥/私钥
     * @param str 待加密字符串
     * @return 密文
     */
    public static String encryptByPublicKey(String str, Key key) throws Exception {
        //RSA加密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] enBytes = cipher.doFinal(str.getBytes());
        return base64Encode(enBytes);
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 明文
     */
    public static String decryptByPrivateKey(String str, String privateKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = base64Decode(str);
        //base64编码的私钥
        byte[] decoded = base64Decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return new String(cipher.doFinal(inputByte));
    }

    /**
     * RSA公钥解密
     *
     * @param str       字符串
     * @param publicKey 公钥
     * @return 明文
     */
    public static String decryptByPublicKey(String str, String publicKey) throws Exception {
        //64位解码加密后的字符串
        byte[] inputByte = base64Decode(str);
        //base64编码的私钥
        byte[] decoded = base64Decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
        //RSA解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return new String(cipher.doFinal(inputByte));
    }

    /**
     * 解密
     *
     * @param key   公钥/私钥
     * @param enStr 加密字符串
     * @return 明文
     */
    public static String decryptByPrivateKey(String enStr, Key key) throws Exception {
        //RSA解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] deBytes = cipher.doFinal(base64Decode(enStr));
        return new String(deBytes);
    }

    /**
     * 计算签名
     *
     * @param data       原始数据
     * @param privateKey RSA私钥
     * @return 签名
     */
    public static String signature(String data, String privateKey) throws Exception {
        //base64编码的私钥
        byte[] decoded = base64Decode(privateKey);
        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        // 用私钥对信息生成数字签名
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(priKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return base64Encode(signature.sign());
    }

    /**
     * 验证签名
     *
     * @param data      原始数据
     * @param publicKey RSA公钥
     * @param sign      私钥签名
     * @return 是否一致
     */
    public static boolean verify(String data, String publicKey, String sign) throws Exception {
        //base64编码的私钥
        byte[] decoded = base64Decode(publicKey);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance(KEY_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(pubKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        // 验证签名是否有效
        return signature.verify(base64Decode(sign));
    }

    /**
     * base64 编码
     *
     * @param bytes 待编码的byte[]
     * @return 编码后的base 64 code
     */
    private static String base64Encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * base64 解码
     *
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     */
    private static byte[] base64Decode(String base64Code) {
        // Base64.NO_WRAP 不换行
        return base64Code == null || "".equals(base64Code) ? null : Base64.decode(base64Code, Base64.NO_WRAP);
    }

}
