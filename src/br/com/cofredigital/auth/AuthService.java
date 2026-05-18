// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.auth;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.util.BcryptUtil;

public class AuthService {
    private UserService userService = new UserService();
    private String foundPassword;

    public User findUser(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return userService.findByEmail(email.trim());
    }

    public String verifyPassword(User u, java.util.List<int[]> mappings) {
        if (tryCombinations(mappings, 0, new StringBuilder(), u.getPasswordHash())) {
            return foundPassword;
        }
        return null;
    }

    private boolean tryCombinations(java.util.List<int[]> mappings, int index,
                                     StringBuilder current, String storedHash) {
        if (index == mappings.size()) {
            String candidate = current.toString();
            if (BcryptUtil.verify(candidate, storedHash)) {
                foundPassword = candidate;
                return true;
            }
            return false;
        }

        int[] pair = mappings.get(index);
        current.append(pair[0]);
        if (tryCombinations(mappings, index + 1, current, storedHash)) return true;
        current.deleteCharAt(current.length() - 1);

        current.append(pair[1]);
        if (tryCombinations(mappings, index + 1, current, storedHash)) return true;
        current.deleteCharAt(current.length() - 1);

        return false;
    }

    public boolean verifyTOTP(User u, String code, String password) throws Exception {
        return new TokenService().validateTokenForUser(u, code, password);
    }

    public void registerPasswordError(User u) {
        userService.registerPasswordError(u);
    }

    public void registerTokenError(User u) {
        userService.registerTokenError(u);
    }

    public void resetErrors(User u) throws Exception {
        userService.resetErrorCounts(u);
        new br.com.cofredigital.database.UserDAO().incrementAccessCount(u);
    }
}
