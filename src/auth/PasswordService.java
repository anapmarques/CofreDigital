package auth;

import util.BcryptUtil;

public class PasswordService {
    public static String hashPassword(String plain) {
        return BcryptUtil.hashpw(plain, 8);
    }
}
