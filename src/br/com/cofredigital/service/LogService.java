package br.com.cofredigital.service;

import br.com.cofredigital.database.LogDAO;

public class LogService {
    private LogDAO dao = new LogDAO();

    // Registra uma mensagem de log
    public void append(int mid, int uid) {
        try { dao.append(mid, (long) uid); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
