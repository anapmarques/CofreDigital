package br.com.cofredigital.auth;

import br.com.cofredigital.util.BcryptUtil;

public class PasswordService {
    public static String hashPassword(String plain) {
        return BcryptUtil.hashpw(plain, 8);
    }
}
