package br.com.cofredigital.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

public class QRCodeUtil {

    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    public static void saveQRCodeImage(String text, int width, int height, Path filePath) throws IOException, WriterException {
        BufferedImage image = generateQRCodeImage(text, width, height);
        ImageIO.write(image, "png", filePath.toFile());
    }

    public static String buildTOTPUri(String email, String base32Secret) {
        return "otpauth://totp/Cofre%20Digital:" + email + "?secret=" + base32Secret + "&issuer=Cofre%20Digital";
    }
}
