CREATE TABLE IF NOT EXISTS Usuarios (
    uid INTEGER PRIMARY KEY,
    senha TEXT NOT NULL UNIQUE,
    chave_secreta TEXT NOT NULL,
    login_name TEXT NOT NULL UNIQUE,
    nome TEXT NOT NULL,
    kid INTEGER,

    FOREIGN KEY (kid) REFERENCES Chaveiro(kid),
    FOREIGN KEY (gid) REFERENCES Grupos(gid)
);

CREATE TABLE IF NOT EXISTS Chaveiro (
    certificado_digital TEXT NOT NULL,
    chave_privada TEXT NOT NULL,
    kid INTEGER PRIMARY KEY,
    uid INTEGER NOT NULL
)

CREATE TABLE IF NOT EXISTS Grupos (
    gid INTEGER PRIMARY KEY,
    nome TEXT NOT NULL UNIQUE
)

CREATE TABLE IF NOT EXISTS Mensagens (
    mid INTEGER PRIMARY KEY,
    mensagem TEXT NOT NULL
)

CREATE TABLE IF NOT EXISTS Registros (
    rid INTEGER PRIMARY KEY,
    mid INTEGER NOT NULL,
    uid INTEGER,
    timestamp TEXT NOT NULL,

    FOREIGN KEY (mid) REFERENCES Mensagens(mid),
    FOREIGN KEY (uid) REFERENCES Usuarios(uid)
)

