// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.crypto;

import javax.crypto.Cipher;

public class EnvelopeUtil {
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] plainData, String password) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, KeyDerivationUtil.deriveKey(password));
        return cipher.doFinal(plainData);
    }

    public static byte[] decrypt(byte[] cipherData, String password) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KeyDerivationUtil.deriveKey(password));
            return cipher.doFinal(cipherData);
        } catch (Exception e) {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KeyDerivationUtil.deriveKeySHA1PRNG(password));
            return cipher.doFinal(cipherData);
        }
    }
}
