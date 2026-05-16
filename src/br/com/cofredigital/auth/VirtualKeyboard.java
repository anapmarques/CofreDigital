package br.com.cofredigital.auth;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VirtualKeyboard {
    private static final int TOTAL_BUTTONS = 5;
    private SecureRandom random = new SecureRandom();
    private List<int[]> buttons;

    public VirtualKeyboard() {
        shuffle();
    }

    public void shuffle() {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) digits.add(i);
        Collections.shuffle(digits, random);
        buttons = new ArrayList<>();
        for (int i = 0; i < TOTAL_BUTTONS; i++) {
            buttons.add(new int[]{digits.get(i * 2), digits.get(i * 2 + 1)});
        }
    }

    public int[] getButtonDigits(int buttonNumber) {
        return buttons.get(buttonNumber - 1);
    }

    public List<int[]> getButtons() {
        return buttons;
    }

    public static int getTotalButtons() {
        return TOTAL_BUTTONS;
    }
}
