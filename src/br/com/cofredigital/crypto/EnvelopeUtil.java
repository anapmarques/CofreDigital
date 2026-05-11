package br.com.cofredigital.crypto;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Utilitário simples de envelope: lê uma chave AES de 16 bytes do arquivo ./vault/index.env
 * e a usa para criptografar/descriptografar segredos pequenos.
 */
public class EnvelopeUtil {
    private static byte[] masterKey = null;

    private static void ensureKey() throws Exception {
        if (masterKey != null) return;
        File f = new File("./vault/index.env");
        if (!f.exists()) throw new IllegalStateException("Master envelope key not found at ./vault/index.env");
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
}
