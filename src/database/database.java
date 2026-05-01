import java.util.HashMap;

public class Database {

    private static HashMap<String, User> users = new HashMap<>();

    public static boolean temUsuarios() {
        return !users.isEmpty();
    }

    public static User buscarUser(String email) {
        return users.get(email);
    }

    public static void salvar(User user) {
        users.put(user.email, user);
    }
}