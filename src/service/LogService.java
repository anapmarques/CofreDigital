package service;

import database.LogDAO;

public class LogService {
    private LogDAO dao = new LogDAO();

    // Registra um evento (apenas mid e uid, sem texto)
    public void logEvent(int mid, Long uid) {
        try { dao.append(mid, uid); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
