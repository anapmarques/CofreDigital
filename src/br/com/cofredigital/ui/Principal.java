package br.com.cofredigital.ui;

import javax.swing.*;
import java.awt.*;
import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.service.VaultService;
import br.com.cofredigital.service.VaultService.VaultFileEntry;
import br.com.cofredigital.service.VaultService.VaultResult;
import br.com.cofredigital.database.LogDAO;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Principal extends JFrame {
    private User usuario;

    public Principal(User usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        if ("administrador".equals(usuario.getGroup())) {
            configurarTelaAdmin();
        } else if ("usuario".equals(usuario.getGroup())) {
            configurarTelaUser();
        } else {
            JOptionPane.showMessageDialog(this, "Grupo invalido.");
            dispose();
        }
    }

    private void configurarTelaAdmin() {
        setLayout(new BorderLayout());

        JLabel labelCabecalho = new JLabel("<html>Login: " + usuario.getEmail() + "<br>" +
            "Grupo: " + usuario.getGroup() + "<br>" +
            "Nome: " + usuario.getUserName() + "</html>");
        labelCabecalho.setHorizontalAlignment(SwingConstants.CENTER);
        labelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelCabecalho, BorderLayout.NORTH);

        JLabel labelCorpo1 = new JLabel("Total de acessos do usuario: " + usuario.getTotalUsers());
        labelCorpo1.setHorizontalAlignment(SwingConstants.CENTER);
        labelCorpo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelCorpo1, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout());

        JButton btnCadastrar = new JButton("Cadastrar um novo usuario");
        JButton btnConsultar = new JButton("Consultar pasta de arquivos secretos");
        JButton btnSair = new JButton("Sair do Sistema");

        btnCadastrar.addActionListener(e -> {
            new LogDAO().addLog(5002, usuario.getEmail(), now());
            String[] grupos = new br.com.cofredigital.database.Database().getGrupos();
            new Cadastro(usuario, grupos).setVisible(true);
            dispose();
        });

        btnConsultar.addActionListener(e -> {
            new LogDAO().addLog(5003, usuario.getEmail(), now());
            consultarVault();
        });
        btnSair.addActionListener(e -> {
            new LogDAO().addLog(5004, usuario.getEmail(), now());
            new Sair(usuario).setVisible(true);
            dispose();
        });

        painelBotoes.add(btnCadastrar);
        painelBotoes.add(btnConsultar);
        painelBotoes.add(btnSair);
        add(painelBotoes, BorderLayout.SOUTH);

        pack();
        new LogDAO().addLog(5001, usuario.getEmail(), now());
    }

    private void configurarTelaUser() {
        setLayout(new BorderLayout());

        JLabel labelCabecalho = new JLabel("<html>Login: " + usuario.getEmail() + "<br>" +
            "Grupo: Usuario<br>" +
            "Nome: " + usuario.getUserName() + "</html>");
        labelCabecalho.setHorizontalAlignment(SwingConstants.CENTER);
        labelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelCabecalho, BorderLayout.NORTH);

        JLabel labelCorpo1 = new JLabel("Total de acessos do usuario: " + usuario.getTotalUsers());
        labelCorpo1.setHorizontalAlignment(SwingConstants.CENTER);
        labelCorpo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelCorpo1, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout());

        JButton btnConsultar = new JButton("Consultar pasta de arquivos secretos");
        JButton btnSair = new JButton("Sair do Sistema");

        btnConsultar.addActionListener(e -> {
            new LogDAO().addLog(5003, usuario.getEmail(), now());
            consultarVault();
        });
        btnSair.addActionListener(e -> {
            new LogDAO().addLog(5004, usuario.getEmail(), now());
            new Sair(usuario).setVisible(true);
            dispose();
        });

        painelBotoes.add(btnConsultar);
        painelBotoes.add(btnSair);
        add(painelBotoes, BorderLayout.SOUTH);

        pack();
        new LogDAO().addLog(5001, usuario.getEmail(), now());
    }

    private void consultarVault() {
        new LogDAO().addLog(7001, usuario.getEmail(), now());

        JTextField folderField = new JTextField(30);
        JPasswordField phraseField = new JPasswordField(20);
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Caminho da pasta:"));
        panel.add(folderField);
        panel.add(new JLabel("Frase secreta:"));
        panel.add(phraseField);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Consultar Arquivos Secretos", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            new LogDAO().addLog(7002, usuario.getEmail(), now());
            return;
        }
        new LogDAO().addLog(7003, usuario.getEmail(), now());

        String folderPath = folderField.getText().trim();
        String phrase = new String(phraseField.getPassword());
        if (folderPath.isEmpty() || phrase.isEmpty()) {
            new LogDAO().addLog(7004, usuario.getEmail(), now());
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String adminPassphrase = UserService.getAdminPassphrase();

        VaultService vs = new VaultService();
        VaultResult vr = vs.listVaultFiles(usuario, folderPath, phrase, adminPassphrase);

        if (!vr.isSuccess()) {
            JOptionPane.showMessageDialog(this, vr.error, "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (vr.files.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum arquivo encontrado para o usuario ou grupo.",
                "Resultado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] colunas = {"Codigo", "Nome", "Dono", "Grupo"};
        Object[][] dados = new Object[vr.files.size()][4];
        for (int i = 0; i < vr.files.size(); i++) {
            VaultFileEntry f = vr.files.get(i);
            dados[i] = new Object[]{f.codigo, f.nome, f.dono, f.grupo};
        }

        JTable table = new JTable(dados, colunas);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        String selectedFile = (String) JOptionPane.showInputDialog(this, scrollPane,
            "Arquivos Disponiveis - Selecione pelo nome",
            JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (selectedFile == null || selectedFile.trim().isEmpty()) return;

        VaultFileEntry selected = null;
        for (VaultFileEntry f : vr.files) {
            if (f.nome.equals(selectedFile.trim())) {
                selected = f;
                break;
            }
        }

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Arquivo nao encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new LogDAO().addLog(7010, usuario.getEmail(), now(), selected.nome);

        if (!selected.dono.equals(usuario.getUserName())) {
            new LogDAO().addLog(7012, usuario.getEmail(), now(), selected.nome);
            JOptionPane.showMessageDialog(this, "Acesso negado: voce nao e o dono do arquivo.",
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new LogDAO().addLog(7011, usuario.getEmail(), now(), selected.nome);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Deseja decriptar o arquivo \"" + selected.nome + "\"?",
            "Confirmacao", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String error = vs.decryptFile(usuario, folderPath, selected.codigo, phrase, adminPassphrase);
        if (error != null) {
            JOptionPane.showMessageDialog(this, "Erro ao decriptar: " + error, "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Arquivo decriptado com sucesso: " + selected.nome + "\nSalvo em: " + selected.nome,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
