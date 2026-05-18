// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.util;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import java.security.SecureRandom;

public class BcryptUtil {
    private static final SecureRandom rnd = new SecureRandom();

    public static String hashpw(String plain, int cost) {
        byte[] salt = new byte[16];
        rnd.nextBytes(salt);
        return OpenBSDBCrypt.generate(plain.toCharArray(), salt, cost);
    }

    public static boolean verify(String plain, String stored) {
        return OpenBSDBCrypt.checkPassword(stored, plain.toCharArray());
    }
}
