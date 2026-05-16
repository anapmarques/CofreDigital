package br.com.cofredigital.security;

import br.com.cofredigital.crypto.EnvelopeUtil;
import br.com.cofredigital.crypto.TOTP;
import br.com.cofredigital.model.User;
import java.nio.charset.StandardCharsets;

public class TokenService {
    public boolean validateTokenForUser(User u, String code, String password) throws Exception {
        if (u.getTotpEncrypted() == null) return false;
        byte[] base32Bytes = EnvelopeUtil.decrypt(u.getTotpEncrypted(), password);
        String base32 = new String(base32Bytes, StandardCharsets.UTF_8);
        TOTP totp = new TOTP(base32, 30);
        return totp.validateCode(code);
    }
}
