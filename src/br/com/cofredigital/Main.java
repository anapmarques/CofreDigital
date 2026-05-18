// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital;

import br.com.cofredigital.service.SystemService;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) throws Exception {
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
        UIManager.put("ScrollBar.arc", 10);
        UIManager.put("Button.margin", new Insets(8, 22, 8, 22));
        UIManager.put("Button.font", UIManager.getFont("Button.font").deriveFont(Font.BOLD, 14f));
        UIManager.put("Label.font", UIManager.getFont("Label.font").deriveFont(13f));
        UIManager.put("TextField.font", UIManager.getFont("TextField.font").deriveFont(14f));
        UIManager.put("PasswordField.font", UIManager.getFont("PasswordField.font").deriveFont(14f));
        UIManager.put("Table.font", UIManager.getFont("Table.font").deriveFont(13f));
        UIManager.put("TableHeader.font", UIManager.getFont("TableHeader.font").deriveFont(Font.BOLD, 13f));

        SystemService.start();
    }
}