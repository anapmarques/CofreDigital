public class SystemService {

    public static void start() throws Exception {

        if (!Database.temUsuarios()) {
            UserService.cadastrarAdministrador();
        } else {
            if (!UserService.validarFraseAdmin()) return;
        }

        while (true) {
            User user = AuthService.login();
            if (user != null) {
                MenuService.show(user);
            }
        }
    }
}