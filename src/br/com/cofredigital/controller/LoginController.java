// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.controller;

import br.com.cofredigital.auth.AuthService;
import br.com.cofredigital.model.User;

public class LoginController {
    private AuthService authService = new AuthService();

    public User findUser(String email) throws Exception {
        return authService.findUser(email);
    }

    public String verifyPassword(User user, java.util.List<int[]> mappings) {
        return authService.verifyPassword(user, mappings);
    }

    public boolean verifyTOTP(User user, String code, String password) throws Exception {
        return authService.verifyTOTP(user, code, password);
    }

    public void registerPasswordError(User user) {
        authService.registerPasswordError(user);
    }

    public void registerTokenError(User user) {
        authService.registerTokenError(user);
    }

    public void resetErrors(User user) throws Exception {
        authService.resetErrors(user);
    }
}
