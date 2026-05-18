// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectionFactory {
    public static final String URL = "jdbc:sqlite:cofre.db";

    static {
        try {
            Connection c = getConnection();
            createTables(c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection(URL);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    private static void createTables(Connection conn) {
        String[] sqls = {
            "CREATE TABLE IF NOT EXISTS Usuarios (" +
            "    uid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    email TEXT NOT NULL UNIQUE," +
            "    senha TEXT NOT NULL," +
            "    frase_secreta TEXT," +
            "    chave_secreta BLOB," +
            "    certificado BLOB," +
            "    chave_privada BLOB," +
            "    kid INTEGER," +
            "    grupo TEXT," +
            "    total_acessos INTEGER DEFAULT 0," +
            "    total_consultas INTEGER DEFAULT 0," +
            "    blocked INTEGER DEFAULT 0," +
            "    blockUntil INTEGER DEFAULT 0," +
            "    passwordErrors INTEGER DEFAULT 0," +
            "    tokenErrors INTEGER DEFAULT 0," +
            "    bloqueado_ultima_vez TEXT," +
            "    total_users INTEGER DEFAULT 0," +
            "    gid INTEGER," +
            "    nome TEXT," +
            "    FOREIGN KEY (gid) REFERENCES Grupos(gid)," +
            "    FOREIGN KEY (kid) REFERENCES Chaveiro(kid)" +
            ")",
            "CREATE TABLE IF NOT EXISTS Chaveiro (" +
            "    kid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    uid INTEGER," +
            "    certificado_digital BLOB," +
            "    chave_privada BLOB," +
            "    FOREIGN KEY (uid) REFERENCES Usuarios(uid)" +
            ")",
            "CREATE TABLE IF NOT EXISTS Grupos (" +
            "    gid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    nome TEXT NOT NULL" +
            ")",
            "CREATE TABLE IF NOT EXISTS Mensagens (" +
            "    mid INTEGER PRIMARY KEY," +
            "    conteudo TEXT" +
            ")",
            "CREATE TABLE IF NOT EXISTS Registros (" +
            "    rid INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    mid INTEGER NOT NULL," +
            "    uid INTEGER," +
            "    arquivo TEXT," +
            "    timestamp TEXT NOT NULL," +
            "    FOREIGN KEY (mid) REFERENCES Mensagens(mid)," +
            "    FOREIGN KEY (uid) REFERENCES Usuarios(uid)" +
            ")"
        };
        for (String sql : sqls) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (Exception e) {
                System.err.println("Error creating table: " + e.getMessage());
            }
        }

        Database.seedData(conn);
        // Migration: add frase_secreta column if missing (for existing databases)
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE Usuarios ADD COLUMN frase_secreta TEXT");
            }
        } catch (Exception e) {
            // Column already exists — ignore
        }

        // Migration: add arquivo column to Registros if missing
        try {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE Registros ADD COLUMN arquivo TEXT");
            }
        } catch (Exception e) {
            // Column already exists — ignore
        }
    }

}
