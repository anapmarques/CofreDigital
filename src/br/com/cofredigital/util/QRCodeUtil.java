package br.com.cofredigital.util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class QRCodeUtil {

    public static BufferedImage generateQRCodeImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, Math.max(12, Math.min(24, height / 10))));
        graphics.drawString(text, 10, height / 2);
        graphics.dispose();
        return image;
    }

    public static void saveQRCodeImage(String text, int width, int height, Path filePath) throws IOException {
        BufferedImage image = generateQRCodeImage(text, width, height);
        ImageIO.write(image, "png", filePath.toFile());
    }

    public static String buildTOTPUri(String email, String base32Secret) {
        return "otpauth://totp/Cofre%20Digital:" + email + "?secret=" + base32Secret;
    }
}
