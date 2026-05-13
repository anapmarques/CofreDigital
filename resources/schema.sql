CREATE OR REPLACE FUNCTION createUserTable()
RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS Usuarios (
        uid INTEGER PRIMARY KEY,
        email TEXT NOT NULL UNIQUE,
        senha TEXT NOT NULL,
        chave_secreta TEXT NOT NULL,
        nome TEXT NOT NULL,
        kid INTEGER NOT NULL,
        gid INTEGER NOT NULL,
        grupo TEXT NOT NULL,
        total_acessos INTEGER NOT NULL,
        total_consultas INTEGER NOT NULL,
        bloqueado_ultima_vez TEXT,

        FOREIGN KEY (kid) REFERENCES Chaveiro(kid),
        FOREIGN KEY (gid) REFERENCES Grupos(gid)
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createChaveiroTable()
RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS Chaveiro (
        certificado_digital TEXT NOT NULL,
        chave_privada TEXT NOT NULL,
        kid INTEGER PRIMARY KEY,
        uid INTEGER NOT NULL
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createGruposTable()
RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS Grupos (
        gid INTEGER PRIMARY KEY,
        nome TEXT
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createMensagensTable()
RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS Mensagens (
        mid INTEGER PRIMARY KEY,
        mensagem TEXT NOT NULL
    );
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION createRegistrosTable()
RETURNS void AS $$
BEGIN
    CREATE TABLE IF NOT EXISTS Registros (
        rid INTEGER PRIMARY KEY,
        mid INTEGER NOT NULL,
        uid INTEGER,
        timestamp TEXT NOT NULL,

        FOREIGN KEY (mid) REFERENCES Mensagens(mid),
        FOREIGN KEY (uid) REFERENCES Usuarios(uid)
    );
END;
$$ LANGUAGE plpgsql;