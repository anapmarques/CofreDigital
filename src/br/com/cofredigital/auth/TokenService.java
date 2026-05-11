package br.com.cofredigital.auth;

import br.com.cofredigital.crypto.TOTP;
import br.com.cofredigital.model.User;

public class TokenService {
    public boolean validateTokenForUser(User u, String code) throws Exception {
        if (u.getTotpEncrypted() == null) return false;
        // decrypt TOTP secret using EnvelopeUtil (AES)
    byte[] secret = br.com.cofredigital.crypto.EnvelopeUtil.decrypt(u.getTotpEncrypted());
    String base32 = br.com.cofredigital.util.Base32.encode(secret);
        TOTP totp = new TOTP(base32, 30);
        return totp.validateCode(code);
    }
}
