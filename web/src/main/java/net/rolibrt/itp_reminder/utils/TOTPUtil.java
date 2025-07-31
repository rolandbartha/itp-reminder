package net.rolibrt.itp_reminder.utils;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TOTPUtil {
    private static final GoogleAuthenticator gAuth;
    public static final String APP_NAME = "ITP Reminder";

    static {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setWindowSize(5)
                .build();
        gAuth = new GoogleAuthenticator(config);
    }

    public static GoogleAuthenticatorKey generateSecretKey() {
        return gAuth.createCredentials();
    }

    public static String getQRBarcodeURL(String username, String secret) {
        return "otpauth://totp/"
                + URLEncoder.encode(APP_NAME + ":" + username, StandardCharsets.UTF_8)
                + "?secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                + "&issuer=" + URLEncoder.encode(APP_NAME, StandardCharsets.UTF_8);
    }

    public static String generateQRCodeImageBase64(String barcodeText, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var bitMatrix = qrCodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            byte[] pngData = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Could not generate QR code", e);
        }
    }

    public static boolean isCodeValid(String base32Secret, int verificationCode) {
        return gAuth.authorize(base32Secret, verificationCode);
    }

    public static String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0]; // In case of multiple forwarded IPs
        }
        return request.getRemoteAddr();
    }
}