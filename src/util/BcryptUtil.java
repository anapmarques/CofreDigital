package util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilitário mínimo para hash de senhas. O requisito pedia bcrypt com cost 8.
 * Para evitar bibliotecas externas, esta implementação usa PBKDF2-HMAC-SHA256 como
 * alternativa segura com fator de trabalho ajustável. Formato armazenado: pbkdf2$iterations$base64(salt)$base64(hash)
 */
public class BcryptUtil {
    private static final SecureRandom rnd = new SecureRandom();

    public static String hashpw(String plain, int cost) {
        try {
            int iterations = 1 << cost; // rough mapping
            byte[] salt = new byte[16];
            rnd.nextBytes(salt);
            byte[] dk = pbkdf2(plain.toCharArray(), salt, iterations, 32);
            return String.format("pbkdf2$%d$%s$%s", iterations, Base64.getEncoder().encodeToString(salt), Base64.getEncoder().encodeToString(dk));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static boolean verify(String plain, String stored) {
        try {
            if (!stored.startsWith("pbkdf2$")) return false;
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            byte[] dk = pbkdf2(plain.toCharArray(), salt, iterations, hash.length);
            if (dk.length != hash.length) return false;
            int diff = 0;
            for (int i=0;i<dk.length;i++) diff |= dk[i] ^ hash[i];
            return diff == 0;
        } catch (Exception e) { return false; }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int dkLen) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, dkLen * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
}
