package Interface;

import model.User;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.*;

public class Cadastro extends JFrame {
    private String loginName;
    private String groupName;
    private String userName;
    private int total_users;
    private String[] groups;

    private User user;

    public Cadastro(String loginName, String groupName, String userName, int total_users, String[] groups) {
        this.loginName = loginName;
        this.groupName = groupName;
        this.userName = userName;
        this.total_users = total_users;
        this.groups = groups;

        openRegisterScreen();
    }

    public Cadastro(User user, String[] groups) {
        this.loginName = user.getEmail();
        this.groupName = user.getGroup();
        this.userName = user.getUserName();
        this.total_users = user.getTotalUsers();
        this.groups = groups;
        this.user = user;

        openRegisterScreen();
    }

    private void openRegisterScreen() {
        setTitle("Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JLabel headerLabel = new JLabel("<html>Login: " + loginName + "<br>" + "Grupo: " + groupName + "<br>" + "Nome: " + userName + "</html>");
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(headerLabel);

        JLabel labelCorpo1 = new JLabel("<html>Total de usuários: " + total_users + "</html>");
        labelCorpo1.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelCorpo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(labelCorpo1);

        // Painel de Cadastro (GridLayout for the form)
        JPanel painelCadastro = new JPanel(new GridLayout(6, 2, 5, 5));
        painelCadastro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblCertificado = new JLabel("Caminho do Certificado:");
        JTextField txtCertificado = new JTextField(20);

        JLabel lblChavePrivada = new JLabel("Caminho da Chave Privada:");
        JTextField txtChavePrivada = new JTextField(20);

        JLabel lblFraseSecreta = new JLabel("Frase Secreta:");
        JPasswordField txtFraseSecreta = new JPasswordField(20);

        JLabel lblGrupo = new JLabel("Grupo:");
        JComboBox<String> comboGrupo = new JComboBox<>(groups);

        JLabel lblSenha = new JLabel("Senha Pessoal:");
        JPasswordField txtSenha = new JPasswordField(10);

        JLabel lblConfirmacaoSenha = new JLabel("Confirmação Senha:");
        JPasswordField txtConfirmacaoSenha = new JPasswordField(10);

        painelCadastro.add(lblCertificado);
        painelCadastro.add(txtCertificado);
        painelCadastro.add(lblChavePrivada);
        painelCadastro.add(txtChavePrivada);
        painelCadastro.add(lblFraseSecreta);
        painelCadastro.add(txtFraseSecreta);
        painelCadastro.add(lblGrupo);
        painelCadastro.add(comboGrupo);
        painelCadastro.add(lblSenha);
        painelCadastro.add(txtSenha);
        painelCadastro.add(lblConfirmacaoSenha);
        painelCadastro.add(txtConfirmacaoSenha);

        contentPane.add(painelCadastro);

        // Painel de Botões
        JPanel painelBotoesCadastro = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.setEnabled(false); // Initially disabled
        JButton btnVoltar = new JButton("Voltar");
        if (user == null){
            btnVoltar.setEnabled(false); // Disable the button if no user is provided (it's the first user)
        }

        // Add DocumentListener to password fields to enable/disable the button based on length
        DocumentListener passwordLengthListener = createPasswordLengthListener(txtSenha, txtConfirmacaoSenha, btnCadastrar, 8);
        txtSenha.getDocument().addDocumentListener(passwordLengthListener);
        txtConfirmacaoSenha.getDocument().addDocumentListener(passwordLengthListener);
        
        // btnCadastrar.addActionListener(e -> {
        //         Cofre.confirmaCadastro(
        //             txtCertificado.getText(),
        //             txtChavePrivada.getText(),
        //             new String(txtFraseSecreta.getPassword()),
        //             (String) comboGrupo.getSelectedItem(),
        //             new String(txtSenha.getPassword()),
        //             new String(txtConfirmacaoSenha.getPassword())
        //         );

        //         Cofre.addLogToDB(loginName, "6002"); 

        //         dispose();
        //     }
        // );

        // btnVoltar.addActionListener(e -> {
        //     Cofre.addLogToDB(loginName, "6010");

        //     Cofre.showMenuPrincipal(user);

        //     dispose();
        // });

        painelBotoesCadastro.add(btnCadastrar);
        
        painelBotoesCadastro.add(btnVoltar);

        contentPane.add(painelBotoesCadastro);

        pack();
    }

    private static DocumentListener createPasswordLengthListener(JPasswordField passwordField,JPasswordField confirmPasswordField, JButton button, int minLength) {
        return new DocumentListener() {
            private void updateButtonState() {
                button.setEnabled(passwordField.getPassword().length >= minLength);
                button.setEnabled(confirmPasswordField.getPassword().length >= minLength);
            }

            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        };
    }
}