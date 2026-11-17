package cs_26_sw_3_10.P3Project.platform.twofactorauthentication;


import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class TotpService {

    private final TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
    private final Base32 base32 = new Base32();
    private final SecureRandom random = new SecureRandom();

    public String generateSecret() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    public String getQrCodeHtml(String appName, String username, String secret) throws Exception {
        String uri = "otpauth://totp/" + enc(appName) + ":" + enc(username)
                + "?secret=" + secret
                + "&issuer=" + enc(appName)
                + "&algorithm=SHA1&digits=6&period=30";

        BitMatrix matrix = new QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, 250, 250);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
        return "<img src=\"data:image/png;base64," + base64 + "\" alt=\"2FA QR code\"/>";
    }

  public boolean validatePin(String pin, String secret) throws Exception {
    if (pin == null || secret == null || !pin.matches("\\d{6}")) return false;

    SecretKey key = new SecretKeySpec(base32.decode(secret), totp.getAlgorithm());
    Instant now = Instant.now();

    for (int offset = -1; offset <= 1; offset++) {
        Instant t = now.plus(totp.getTimeStep().multipliedBy(offset));
        String expected = String.format("%06d", totp.generateOneTimePassword(key, t));
        if (MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                  pin.getBytes(StandardCharsets.UTF_8))) {
            return true;
        }
    }
    return false;
}

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}