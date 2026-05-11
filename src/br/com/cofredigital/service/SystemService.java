package br.com.cofredigital.service;

import br.com.cofredigital.auth.AuthService;
import br.com.cofredigital.database.Database;
import br.com.cofredigital.model.User;


public class SystemService {

    public static void start() throws Exception {

        UserService userService = new UserService();

        if (!Database.temUsuarios()) {
            userService.createAdminInteractive();
        } else {
            if (!userService.validateAdminPhrase("phrase")) return;
        }

        AuthService authService = new AuthService();
        while (true) {
            User user = authService.login();
            if (user != null) {
                MenuService.show(user);
            }
        }
    }
}