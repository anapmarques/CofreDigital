package auth;

import model.User;
import service.UserService;
import util.BcryptUtil;
import util.Input;
import auth.TokenService;

/**
 * Garante o fluxo de autenticação combinando senha e TOTP.
 */
public class AuthService {
    private UserService userService = new UserService();
    private TokenService tokenService = new TokenService();

    public boolean authenticateInteractive() throws Exception {
        System.out.print("Email (from certificate): ");
        String email = Input.readLine();
        User u = userService.findByEmail(email);
        if (u == null) {
            System.out.println("Usuário não encontrado");
            return false;
        }

        if (u.isCurrentlyBlocked()) {
            System.out.println("Usuário bloqueado até: " + u.getBlockUntil());
            return false;
        }

        System.out.print("Senha: ");
        String pw = Input.readLineMasked();
        boolean passOk = BcryptUtil.verify(pw, u.getPasswordHash());
        if (!passOk) {
            userService.registerPasswordError(u);
            System.out.println("Senha incorreta");
            return false;
        }

        System.out.print("TOTP (6 dígitos): ");
        String code = Input.readLine();
        if (!tokenService.validateTokenForUser(u, code)) {
            userService.registerTokenError(u);
            System.out.println("TOTP inválido");
            return false;
        }

        userService.resetErrorCounts(u);
        System.out.println("Autenticado com sucesso: " + email);
        return true;
    }
}
