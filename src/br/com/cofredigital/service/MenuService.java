package br.com.cofredigital.service;

import java.util.Scanner;

import br.com.cofredigital.model.User;

public class MenuService {

    public static void show(User user) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            System.out.println("\n--- Menu Principal ---");
            System.out.println("Usuário: " + user.getUserName());
            System.out.println("1. Gerenciar chaves (KeyStore)");
            System.out.println("2. Visualizar logs (Log)");
            System.out.println("3. Gerenciar usuários (User)");
            System.out.println("4. Gerenciar cofre (Vault)");
            System.out.println("5. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        System.out.println("-> Chamando KeyStoreService...");
                        // KeyStoreService.menu(user); // Implementação futura
                        break;
                    case 2:
                        System.out.println("-> Chamando LogService...");
                        // LogService.menu(user); // Implementação futura
                        break;
                    case 3:
                        System.out.println("-> Chamando UserService...");
                        // UserService.menu(user); // Implementação futura
                        break;
                    case 4:
                        System.out.println("-> Chamando VaultService...");
                        // VaultService.menu(user); // Implementação futura
                        break;
                    case 5:
                        System.out.println("Saindo...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, insira um número.");
            }
        }
    }
}