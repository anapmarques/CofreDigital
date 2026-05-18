// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class KeyDerivationUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final int AES_KEY_LENGTH = 256;
    private static final String PRNG_ALGORITHM = "SHA1PRNG";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final byte[] PBKDF2_SALT = "CofreDigital2024".getBytes(StandardCharsets.UTF_8);

    public static SecretKey deriveKey(String password) throws Exception {
        return deriveKeyPBKDF2(password);
    }

    public static SecretKey deriveKeyPBKDF2(String password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), PBKDF2_SALT, PBKDF2_ITERATIONS, AES_KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), AES_ALGORITHM);
    }

    public static SecretKey deriveKeySHA1PRNG(String password) throws Exception {
        SecureRandom random = SecureRandom.getInstance(PRNG_ALGORITHM);
        random.setSeed(password.getBytes(StandardCharsets.UTF_8));
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(AES_KEY_LENGTH, random);
        return keyGenerator.generateKey();
    }

    public static SecretKey deriveKeyWithFallback(String password) throws Exception {
        try {
            return deriveKeyPBKDF2(password);
        } catch (Exception e) {
            return deriveKeySHA1PRNG(password);
        }
    }

    public static byte[][] evpBytesToKey(String password, int keyLen, int ivLen, String hashAlg) throws Exception {
        MessageDigest md = MessageDigest.getInstance(hashAlg);
        byte[] passBytes = password.getBytes(StandardCharsets.UTF_8);
        int mdLen = md.getDigestLength();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] last = null;
        int produced = 0;

        while (produced < keyLen + ivLen) {
            if (last != null) {
                md.update(last);
            }
            md.update(passBytes);
            byte[] hash = md.digest();
            output.write(hash, 0, Math.min(hash.length, keyLen + ivLen - produced));
            produced += hash.length;
            last = hash;
        }

        byte[] all = output.toByteArray();
        byte[] key = new byte[keyLen];
        byte[] iv = new byte[ivLen];
        System.arraycopy(all, 0, key, 0, keyLen);
        System.arraycopy(all, keyLen, iv, 0, ivLen);
        return new byte[][]{key, iv};
    }
}
