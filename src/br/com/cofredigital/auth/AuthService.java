package br.com.cofredigital.auth;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.util.BcryptUtil;
import br.com.cofredigital.util.Input;

/**
 * Garante o fluxo de autenticação combinando senha e TOTP.
 */
public class AuthService {
    private UserService userService = new UserService();

    public User login() throws Exception {
        System.out.print("Email (from certificate): ");
        String email = Input.readLine();
        User u = userService.findByEmail(email);
        if (u == null) {
            System.out.println("Usuário não encontrado");
            return null;
        }

        if (u.isCurrentlyBlocked()) {
            System.out.println("Usuário bloqueado até: " + u.getBlockUntil());
            return null;
        }

        System.out.print("Senha: ");
        String pw = Input.readLineMasked();
        boolean passOk = BcryptUtil.verify(pw, u.getPasswordHash());
        if (!passOk) {
            userService.registerPasswordError(u);
            System.out.println("Senha incorreta");
            return null;
        }

        System.out.print("TOTP (6 dígitos): ");
        String code = Input.readLine();
        try {
            if (!new TokenService().validateTokenForUser(u, code)) {
                userService.registerTokenError(u);
                System.out.println("TOTP inválido");
                return null;
            }
        } catch (Exception e) {
            System.out.println("Falha ao validar TOTP");
            return null;
        }

        userService.resetErrorCounts(u);
        System.out.println("Autenticado com sucesso: " + email);
        return u;
    }
}
