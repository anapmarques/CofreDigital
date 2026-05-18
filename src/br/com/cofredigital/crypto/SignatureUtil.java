// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.crypto;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utilitário para assinatura RSA (SHA1withRSA) conforme requisitos.
 */
public class SignatureUtil {
    public static byte[] sign(byte[] privateKeyBytes, byte[] data) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        return sign(pk, data);
    }

    public static byte[] sign(PrivateKey pk, byte[] data) throws Exception {
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(pk);
        sig.update(data);
        return sig.sign();
    }

    public static boolean verify(byte[] certBytes, byte[] data, byte[] signatureBytes) throws Exception {
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(certBytes);
        java.security.cert.Certificate cert = cf.generateCertificate(in);
        PublicKey pub = cert.getPublicKey();
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pub);
        sig.update(data);
        return sig.verify(signatureBytes);
    }

    public static PrivateKey decryptPrivateKey(byte[] encryptedKeyBytes, String passphrase) throws Exception {
        byte[] decrypted = tryDecrypt(encryptedKeyBytes, passphrase);
        String text = new String(decrypted, StandardCharsets.US_ASCII).trim();
        if (text.startsWith("-----BEGIN")) {
            decrypted = pemToDer(text);
        }
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(decrypted));
    }

    private static byte[] tryDecrypt(byte[] encryptedKeyBytes, String passphrase) throws Exception {
        byte[] derBytes = encryptedKeyBytes;
        boolean isPem = false;

        String text = new String(encryptedKeyBytes, StandardCharsets.US_ASCII).trim();
        if (text.startsWith("-----BEGIN")) {
            derBytes = pemToDer(text);
            isPem = true;
        }

        Exception lastError = null;

        if (isPem || isEncryptedPKCS8(derBytes)) {
            try {
                return decryptPKCS8(derBytes, passphrase);
            } catch (Exception e) {
                lastError = e;
            }
        }

        try {
            return decryptAES(encryptedKeyBytes, passphrase);
        } catch (Exception e) {
            if (lastError == null) lastError = e;
        }

        if (isPem) {
            try {
                return decryptAES(derBytes, passphrase);
            } catch (Exception e) {
                if (lastError == null) lastError = e;
            }
        }

        try {
            return decryptOpenSSL(encryptedKeyBytes, passphrase);
        } catch (Exception e) {
            if (lastError == null) lastError = e;
        }

        if (isPem) {
            try {
                return decryptOpenSSL(derBytes, passphrase);
            } catch (Exception e) {
                if (lastError == null) lastError = e;
            }
        }

        if (lastError != null) throw lastError;
        throw new Exception("Nao foi possivel decifrar a chave privada.");
    }

    private static byte[] decryptPKCS8(byte[] derBytes, String passphrase) throws Exception {
        EncryptedPrivateKeyInfo epki = new EncryptedPrivateKeyInfo(derBytes);
        String algName = epki.getAlgName();
        AlgorithmParameters algParams = epki.getAlgParameters();

        String[] factoryNames = {algName, "PBE", "PBKDF2WithHmacSHA256"};
        String[] cipherNames = {algName, "PBES2", "PBEWithHmacSHA256AndAES_256", "PBEWithHmacSHA1AndAES_256", "PBEWithHmacSHA1AndDESede"};

        for (String fn : factoryNames) {
            for (String cn : cipherNames) {
                try {
                    SecretKeyFactory skf = SecretKeyFactory.getInstance(fn);
                    PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray());
                    SecretKey key = skf.generateSecret(spec);
                    Cipher cipher = Cipher.getInstance(cn);
                    if (algParams != null) {
                        cipher.init(Cipher.DECRYPT_MODE, key, algParams);
                    } else {
                        cipher.init(Cipher.DECRYPT_MODE, key);
                    }
                    return cipher.doFinal(epki.getEncryptedData());
                } catch (Exception ignored) {
                }
            }
        }
        throw new Exception("Nao foi possivel decifrar a chave privada (PKCS#8).");
    }

    private static byte[] decryptAES(byte[] data, String passphrase) throws Exception {
        try {
            SecretKey aesKey = KeyDerivationUtil.deriveKey(passphrase);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            SecretKey aesKey = KeyDerivationUtil.deriveKeySHA1PRNG(passphrase);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return cipher.doFinal(data);
        }
    }

    private static byte[] decryptOpenSSL(byte[] encryptedData, String passphrase) throws Exception {
        Exception lastError = null;
        String[] hashAlgs = {"SHA-256", "MD5"};
        for (String hashAlg : hashAlgs) {
            try {
                byte[][] keyIv = KeyDerivationUtil.evpBytesToKey(passphrase, 32, 16, hashAlg);
                SecretKeySpec key = new SecretKeySpec(keyIv[0], "AES");
                IvParameterSpec iv = new IvParameterSpec(keyIv[1]);
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, key, iv);
                byte[] plaintext = cipher.doFinal(encryptedData);
                String text = new String(plaintext, StandardCharsets.US_ASCII).trim();
                if (text.startsWith("-----BEGIN")) {
                    return pemToDer(text);
                }
                return plaintext;
            } catch (Exception e) {
                lastError = e;
            }
        }
        if (lastError != null) throw lastError;
        throw new Exception("Nao foi possivel decifrar (OpenSSL).");
    }

    private static boolean isEncryptedPKCS8(byte[] derBytes) {
        try {
            new EncryptedPrivateKeyInfo(derBytes);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pemToDer(String pemText) throws Exception {
        StringBuilder base64 = new StringBuilder();
        for (String line : pemText.split("\n")) {
            line = line.trim();
            if (line.startsWith("-----BEGIN") || line.startsWith("-----END")) continue;
            base64.append(line);
        }
        return Base64.getDecoder().decode(base64.toString());
    }
}
