package application;

import service.UserService;
import service.LogService;
import service.VaultService;
import util.Input;

/**
 * Ponto de entrada da aplicação. Mantém o main enxuto e delega a lógica para os serviços.
 */
public class Application {
    public static void main(String[] args) throws Exception {
    // Inicializa serviços
        UserService userService = new UserService();
        LogService logService = new LogService();
        VaultService vaultService = new VaultService();

        System.out.println("Cofre Digital - Inicializando...");

        if (!userService.hasAnyUser()) {
            System.out.println("Primeira execução detectada. Crie um usuário administrador.");
            userService.createAdminInteractive();
        } else {
            System.out.println("Sistema já inicializado. Autentique-se como admin para abrir sessão.");
            // Valida a frase secreta do administrador
            System.out.print("Informe a frase secreta do admin: ");
            String phrase = Input.readLine();
            if (!userService.validateAdminPhrase(phrase)) {
                System.err.println("Frase secreta inválida. Encerrando.");
                return;
            }
        }

    // Loop simples para operações via terminal
        while (true) {
            System.out.println("\nEscolha ação: 1=login, 2=criar-usuario, 3=listar-cofre, 0=sair");
            String opt = Input.readLine();
            if ("0".equals(opt)) break;
            switch (opt) {
                case "1":
                    //userService.loginInteractive();
                    break;
                case "2":
                    userService.registerInteractive();
                    break;
                case "3":
                    vaultService.listVaultFilesInteractive();
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }

        System.out.println("Saindo...");
    }
}
