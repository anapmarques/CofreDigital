package br.com.cofredigital.crypto;

import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Utilitário simples de envelope: lê uma chave AES de 16 bytes do arquivo ./vault/Files/index.env
 * e a usa para criptografar/descriptografar segredos pequenos.
 */
public class EnvelopeUtil {
    private static byte[] masterKey = null;

    private static void ensureKey() throws Exception {
        if (masterKey != null) return;
        File f = new File("./vault/Files/index.env");
        if (!f.exists()) throw new IllegalStateException("Master envelope key not found at ./vault/Files/index.env");
        byte[] data = Files.readAllBytes(f.toPath());
        // if file length >=16, take first 16 bytes
        if (data.length < 16) throw new IllegalStateException("index.env must contain at least 16 bytes");
        masterKey = Arrays.copyOf(data, 16);
    }

    public static byte[] encrypt(byte[] plain) throws Exception {
        ensureKey();
        return AESUtil.encrypt(masterKey, plain);
    }

    public static byte[] decrypt(byte[] cipher) throws Exception {
        ensureKey();
        return AESUtil.decrypt(masterKey, cipher);
    }

    public String generateTokenKey() {
        final int TOKEN_KEY_LENGTH = 20;

        try{
            byte[] tokenKey = new byte[TOKEN_KEY_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(tokenKey);

            Base32 base32 = new Base32(Base32.Alphabet.BASE32, true, false);
            String base32token = base32.toString(tokenKey);
            System.out.println("Base32 token key: " + base32token);
            return base32token;
        } catch (Exception e) {
            System.out.println("Error generating token key" + e.getMessage());
        }

        return null;
    }
    
    public static byte[] generateEncryptedTokenKey(String senha, String base32token) {
        int AES_KEY_LENGTH = 256;
        String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
        String AES_ALGORITHM = "AES";
        String PRNG_ALGORITHM = "SHA1PRNG";

        try {
            Base32 base32 = new Base32(Base32.Alphabet.BASE32, true, false);
            byte[] tokenKey = base32.fromString(base32token);

            SecureRandom random = SecureRandom.getInstance(PRNG_ALGORITHM);
            random.setSeed(senha.getBytes());

            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_LENGTH, random);
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedTokenKey = cipher.doFinal(tokenKey);

            System.out.println("Encrypted token key: " + base32.toString(encryptedTokenKey));

            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedTokenKey = cipher.doFinal(encryptedTokenKey);
            System.out.println("Decrypted token key: " + base32.toString(decryptedTokenKey));

            return encryptedTokenKey;
        } catch (Exception e) {
            System.out.println("Error generating encrypted token key: " + e.getMessage());
        }

        return null;
    }

    public String decryptTokenKey(String senha, byte[] encryptedTokenKey) {
        int AES_KEY_LENGTH = 256;
        String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
        String AES_ALGORITHM = "AES";
        String PRNG_ALGORITHM = "SHA1PRNG";

        try {
            System.out.println("Senha: " + senha);

            SecureRandom random = SecureRandom.getInstance(PRNG_ALGORITHM);
            random.setSeed(senha.getBytes());

            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_LENGTH, random);
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedTokenKey = cipher.doFinal(encryptedTokenKey);

            Base32 base32 = new Base32(Base32.Alphabet.BASE32, true, false);
            String base32token = base32.toString(decryptedTokenKey);
            System.out.println("Decrypted token key: " + base32token);
            return base32token;
        } catch (Exception e) {
            System.out.println("Error decrypting token key: " + e.getMessage());
        }

        return null;
    }
}
