package com.nccgroup.collaboratorauth.utilities;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public class Encryption {
    public static byte[] aesEncryptRequest(String secret, String request) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidCipherTextException {
        PBEKeySpec keySpec = new PBEKeySpec(secret.toCharArray(), "CollaboratorAuth".getBytes(), 50, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
        SecretKeySpec secretKey = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
        final byte[] key = secretKey.getEncoded();
        final byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        KeyParameter keyParameter = new KeyParameter(key);
        CipherParameters cipherParameters = new ParametersWithIV(keyParameter, iv);

        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        cipher.init(true, cipherParameters);

        byte[] plainData = request.getBytes();
        byte[] output = new byte[cipher.getOutputSize(request.getBytes().length)];
        int len = cipher.processBytes(plainData, 0, plainData.length, output, 0);
        len += cipher.doFinal(output, len);

        return Arrays.concatenate(iv, output);
    }


    public static String aesDecryptRequest(String secret, byte[] encrypted) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidCipherTextException {
        PBEKeySpec keySpec = new PBEKeySpec(secret.toCharArray(), "CollaboratorAuth".getBytes(), 50, 256);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
        SecretKeySpec secretKey = new SecretKeySpec(keyFactory.generateSecret(keySpec).getEncoded(), "AES");
        final byte[] key = secretKey.getEncoded();
        final byte[] iv = Arrays.copyOfRange(encrypted, 0, 16);
        final byte[] data = Arrays.copyOfRange(encrypted, 16, encrypted.length);

        KeyParameter keyParameter = new KeyParameter(key);
        CipherParameters cipherParameters = new ParametersWithIV(keyParameter, iv);

        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        cipher.init(false, cipherParameters);

        byte[] output = new byte[cipher.getOutputSize(data.length)];
        int len = cipher.processBytes(data, 0, data.length, output, 0);
        len += cipher.doFinal(output, len);

        return new String(Arrays.copyOfRange(output, 0, len));
    }
}