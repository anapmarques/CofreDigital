package br.com.cofredigital.ui;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.database.LogDAO;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sair extends JFrame {
    private User usuario;

    public Sair(User usuario) {
        this.usuario = usuario;

        setTitle("Tela de Sa\u00edda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        String grupoDisplay = "usuario".equals(usuario.getGroup()) ? "Usu\u00e1rio" : usuario.getGroup();
        JLabel labelCabecalho = new JLabel("<html>Login: " + usuario.getEmail() + "<br>" +
            "Grupo: " + grupoDisplay + "<br>" +
            "Nome: " + usuario.getUserName() + "</html>");
        labelCabecalho.setHorizontalAlignment(SwingConstants.CENTER);
        labelCabecalho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(labelCabecalho, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        JLabel labelCorpo1 = new JLabel("Total de acessos do usu\u00e1rio: " + usuario.getTotalUsers());
        labelCorpo1.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelCorpo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bodyPanel.add(labelCorpo1);

        JLabel labelMensagem = new JLabel("Pressione o bot\u00e3o Encerrar Sess\u00e3o ou o bot\u00e3o Encerrar Sistema para confirmar.");
        labelMensagem.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelMensagem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bodyPanel.add(labelMensagem);

        add(bodyPanel, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new FlowLayout());

        JButton btnEncerrarSessao = new JButton("Encerrar Sess\u00e3o");
        JButton btnEncerrarSistema = new JButton("Encerrar Sistema");
        JButton btnVoltar = new JButton("< Voltar de Sair para o Menu Principal");

        btnEncerrarSessao.addActionListener(e -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                new LogDAO().addLog(8002, usuario.getEmail(), timestamp);
            } catch (Exception ex) {
            }
            dispose();
            new Login().setVisible(true);
        });

        btnEncerrarSistema.addActionListener(e -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                new LogDAO().addLog(8003, usuario.getEmail(), timestamp);
            } catch (Exception ex) {
            }
            UserService.clearAdminPassphrase();
            System.exit(0);
        });

        btnVoltar.addActionListener(e -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            try {
                new LogDAO().addLog(8004, usuario.getEmail(), timestamp);
            } catch (Exception ex) {
            }
            new Principal(usuario).setVisible(true);
            dispose();
        });

        painelBotoes.add(btnEncerrarSessao);
        painelBotoes.add(btnEncerrarSistema);
        painelBotoes.add(btnVoltar);

        add(painelBotoes, BorderLayout.SOUTH);

        pack();
    }
}
