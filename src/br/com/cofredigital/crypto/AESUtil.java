// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Auxiliar para AES/ECB/PKCS5Padding. Observação: ECB usado por requisito (não recomendado para dados em massa).
 */
public class AESUtil {
    private static final String ALGO = "AES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] key16, byte[] data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(key16, "AES");
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c.doFinal(data);
    }

    public static byte[] decrypt(byte[] key16, byte[] data) throws Exception {
        SecretKeySpec key = new SecretKeySpec(key16, "AES");
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        return c.doFinal(data);
    }
}
