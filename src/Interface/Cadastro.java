import javax.swing.*;
import java.awt.*;

public class Cadastro extends JFrame {
    public static void main(String[] args) {
        // Criar o JFrame (janela)
        JFrame frame = new JFrame("Exemplo de Caixa de Texto");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new FlowLayout());

        // Criar o JTextField com 15 colunas de tamanho
        JTextField campo_email = new JTextField(15);
        JTextField campo_senha = new JTextField(15);
        
        // Adicionar o campo ao JFrame
        frame.add(campo_email);
        frame.add(campo_senha);

        // Criar um botão para pegar o texto
        JButton botao = new JButton("Cadastrar");
        botao.addActionListener(e -> {
            String email = campo_email.getText();
            String senha = campo_senha.getText();
            System.out.println("Texto digitado: " + email + " " + senha);
        });
        frame.add(botao);

        frame.setVisible(true);

    }
}