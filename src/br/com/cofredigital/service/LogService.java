// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.service;

import br.com.cofredigital.database.LogDAO;

public class LogService {
    private LogDAO dao = new LogDAO();

    // Registra uma mensagem de log
    public void append(int mid, String timestamp) {
        try { dao.addLog(mid, timestamp); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
