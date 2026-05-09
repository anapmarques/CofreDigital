package service;

import database.Database;
import model.User;
import auth.AuthService;

public class SystemService {

    public static void start() throws Exception {

        UserService userService = new UserService();

        if (!Database.temUsuarios()) {
            userService.createAdminInteractive();
        } else {
            if (!userService.validateAdminPhrase("phrase")) return;
        }

        // while (true) {
        //     User user = AuthService.login();
        //     if (user != null) {
        //         MenuService.show(user);
        //     }
        // }
    }
}