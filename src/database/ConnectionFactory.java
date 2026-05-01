package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConnectionFactory {
    private static final String URL = "jdbc:sqlite:cofre.db";

    static {
        try {
            // Inicializa o DB e cria tabelas a partir de resources/schema.sql se presente
            Connection c = getConnection();
            String schema = new String(Files.readAllBytes(Paths.get("resources/schema.sql")));
            Statement st = c.createStatement();
            for (String s : schema.split(";")) {
                String t = s.trim();
                if (!t.isEmpty()) st.execute(t);
            }
            st.close();
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }
}
