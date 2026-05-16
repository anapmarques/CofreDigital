package br.com.cofredigital.controller;

import br.com.cofredigital.service.SystemService;

public class SystemController {
    public void start() throws Exception {
        SystemService.start();
    }
}
