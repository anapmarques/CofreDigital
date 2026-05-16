package br.com.cofredigital.security;

import br.com.cofredigital.util.BcryptUtil;

public class PasswordService {
    public String hashPassword(String plain) {
        return BcryptUtil.hashpw(plain, 8);
    }

    public boolean verifyPassword(String plain, String storedHash) {
        return BcryptUtil.verify(plain, storedHash);
    }
}
