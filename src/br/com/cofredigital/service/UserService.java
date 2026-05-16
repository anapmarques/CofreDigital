package br.com.cofredigital.service;

import br.com.cofredigital.database.UserDAO;
import br.com.cofredigital.model.User;
import br.com.cofredigital.util.BcryptUtil;
import java.security.SecureRandom;
import br.com.cofredigital.database.Database;

public class UserService {
    private UserDAO dao = new UserDAO();
    private SecureRandom rnd = new SecureRandom();
    private static Database database = new Database();
    private static String adminPassphrase = null;

    public boolean hasAnyUser() {
        try { return dao.hasAny(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public boolean validateAdminPhrase(String phrase) {
        try {
            String storedHash = dao.getAdminFraseSecreta();
            if (storedHash == null) return false;
            boolean match = BcryptUtil.verify(phrase, storedHash);
            if (match) {
                adminPassphrase = phrase;
            }
            return match;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getAdminPassphrase() {
        return adminPassphrase;
    }

    public static void clearAdminPassphrase() {
        adminPassphrase = null;
    }

    public static String[] getGrupos() {
        return database.getGrupos();
    }

    public User findByEmail(String email) {
        try { return dao.findByEmail(email); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void registerPasswordError(User u) {
        u.setPasswordErrors(u.getPasswordErrors() + 1);
        if (u.getPasswordErrors() >= 3) {
            u.setBlocked(true);
            u.setBlockUntil(System.currentTimeMillis() + 2 * 60 * 1000);
            u.setPasswordErrors(0);
        }
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void registerTokenError(User u) {
        u.setTokenErrors(u.getTokenErrors() + 1);
        if (u.getTokenErrors() >= 3) {
            u.setBlocked(true);
            u.setBlockUntil(System.currentTimeMillis() + 2 * 60 * 1000);
            u.setTokenErrors(0);
        }
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void resetErrorCounts(User u) {
        u.setPasswordErrors(0); u.setTokenErrors(0); u.setBlocked(false); u.setBlockUntil(0);
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
