package br.com.cofredigital.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionFactory {
    public static final String URL = "jdbc:sqlite:cofre.db";

    static {
        try {
            // Inicializa o DB e cria tabelas a partir de resources/schema.sql se presente
            Connection c = getConnection();

            if (c != null) {
                System.out.println("Connected to SQLite database.");
            }
            else {
                System.out.println("Failed to connect to SQLite database.");
            }

            createTables(c);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL);
    }

    private static void createTables(Connection conn) {
        createUserTable(conn);
        createChaveiroTable(conn);
        createGruposTable(conn);
        createMensagensTable(conn);
        createRegistrosTable(conn);
    }

    private static void createUserTable(Connection conn) {
        try {
            CallableStatement stmt = conn.prepareCall("CALL createUserTable()");
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error creating User table: " + e.getMessage());
        }
    }

    private static void createChaveiroTable(Connection conn) {
        try {
            CallableStatement stmt = conn.prepareCall("CALL createChaveiroTable()");
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error creating Chaveiro table: " + e.getMessage());
        }
    }

    private static void createGruposTable(Connection conn) {
        try {
            CallableStatement stmt = conn.prepareCall("CALL createGruposTable()");
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error creating Grupos table: " + e.getMessage());
        }
    }

    private static void createMensagensTable(Connection conn) {
        try {
            CallableStatement stmt = conn.prepareCall("CALL createMensagensTable()");
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error creating Mensagens table: " + e.getMessage());
        }
    }

    private static void createRegistrosTable(Connection conn) {
        try {
            CallableStatement stmt = conn.prepareCall("CALL createRegistrosTable()");
            stmt.execute();
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error creating Registros table: " + e.getMessage());
        }
    }

}
