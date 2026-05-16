package br.com.cofredigital.ui;

import br.com.cofredigital.auth.VirtualKeyboard;
import br.com.cofredigital.util.BcryptUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VirtualKeyboardPanel extends JPanel {
    private final VirtualKeyboard kb;
    private final List<int[]> mappings;
    private final JLabel stepLabel;
    private final int passwordLength;
    private final JDialog dialog;
    private int currentStep;

    public VirtualKeyboardPanel(VirtualKeyboard kb, List<int[]> mappings,
                                 JLabel stepLabel, int passwordLength, JDialog dialog) {
        this.kb = kb;
        this.mappings = mappings;
        this.stepLabel = stepLabel;
        this.passwordLength = passwordLength;
        this.dialog = dialog;
        this.currentStep = mappings.size();
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        rebuild();
    }

    private void rebuild() {
        removeAll();
        for (int i = 0; i < VirtualKeyboard.getTotalButtons(); i++) {
            int[] digits = kb.getButtonDigits(i + 1);
            JButton btn = new JButton("[" + digits[0] + "][" + digits[1] + "]");
            btn.setFont(new Font("Monospaced", Font.BOLD, 20));
            btn.setPreferredSize(new Dimension(100, 60));
            btn.addActionListener(e -> onButtonClick(digits));
            add(btn);
        }
        revalidate();
        repaint();
    }

    private void onButtonClick(int[] digits) {
        mappings.add(new int[]{digits[0], digits[1]});
        currentStep++;

        if (currentStep >= passwordLength) {
            dialog.dispose();
            return;
        }

        kb.shuffle();
        stepLabel.setText("Digito " + (currentStep + 1) + " de " + passwordLength);
        rebuild();
    }

    public static String showPasswordDialog(Component parent, int passwordLength, String storedHash) {
        JFrame owner = null;
        if (parent instanceof JFrame) {
            owner = (JFrame) parent;
        } else if (parent != null) {
            Window w = SwingUtilities.getWindowAncestor(parent);
            if (w instanceof JFrame) owner = (JFrame) w;
        }

        JDialog dialog = new JDialog(owner, "Teclado Virtual", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel stepLabel = new JLabel("Digito 1 de " + passwordLength, SwingConstants.CENTER);
        stepLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        mainPanel.add(stepLabel, BorderLayout.NORTH);

        List<int[]> mappings = new ArrayList<>();
        VirtualKeyboard kb = new VirtualKeyboard();
        VirtualKeyboardPanel panel = new VirtualKeyboardPanel(kb, mappings, stepLabel, passwordLength, dialog);

        mainPanel.add(panel, BorderLayout.CENTER);
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true); // blocks until dialog is disposed

        if (mappings.size() < passwordLength) return null;

        return tryCombinations(mappings, 0, new StringBuilder(), storedHash);
    }

    private static String tryCombinations(List<int[]> mappings, int index,
                                           StringBuilder current, String storedHash) {
        if (index == mappings.size()) {
            String candidate = current.toString();
            if (BcryptUtil.verify(candidate, storedHash)) {
                return candidate;
            }
            return "";
        }
        int[] pair = mappings.get(index);
        current.append(pair[0]);
        String r = tryCombinations(mappings, index + 1, current, storedHash);
        if (r != null) return r;
        current.deleteCharAt(current.length() - 1);

        current.append(pair[1]);
        r = tryCombinations(mappings, index + 1, current, storedHash);
        if (r != null) return r;
        current.deleteCharAt(current.length() - 1);

        return "";
    }
}
