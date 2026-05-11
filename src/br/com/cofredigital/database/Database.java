package br.com.cofredigital.database;

import br.com.cofredigital.model.User;
import java.util.HashMap;
import java.sql.*;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:cofre.db";
    private static final String [] GROUP_NAMES = {"administrador", "usuario"};

    private static HashMap<String, User> users = new HashMap<>();

    public static boolean temUsuarios() {
        return !users.isEmpty();
    }

    public static User buscarUser(String email) {
        return users.get(email);
    }

    public static void salvar(User user) {
        users.put(user.getEmail(), user);
    }

    public String[] getGrupos() {
        String query = "SELECT nome FROM Grupos";
        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            String[] grupos = new String[GROUP_NAMES.length];
            int i = 0;
            while (rs.next()) {
                grupos[i++] = rs.getString("nome");
            }
            return grupos;
        } catch (SQLException e) {
            System.err.println("Error getting groups: " + e.getMessage());
        }
        return null;
    }
}