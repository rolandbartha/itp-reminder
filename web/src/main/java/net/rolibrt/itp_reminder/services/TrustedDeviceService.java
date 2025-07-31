package net.rolibrt.itp_reminder.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.rolibrt.itp_reminder.models.TrustedDevice;
import net.rolibrt.itp_reminder.repositories.TrustedDeviceRepository;
import net.rolibrt.itp_reminder.utils.TOTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua_parser.Client;
import ua_parser.OS;
import ua_parser.Parser;
import ua_parser.UserAgent;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@EnableScheduling
@Service
public class TrustedDeviceService {
    private static final Logger logger = LoggerFactory.getLogger(TrustedDeviceService.class);
    private static final Parser parser = new Parser();

    @Autowired
    private TrustedDeviceRepository repository;

    @Scheduled(cron = "0 0 3 * * *") // Runs every day at 3 AM
    public void deleteExpiredTokens() {
        logger.info("deleting expired tokens");
        repository.deleteAllExpired(Instant.now());
    }

    public String createToken(HttpServletRequest request, Long userId, Duration duration) {
        String token = UUID.randomUUID().toString();

        String userAgentString = request.getHeader("User-Agent");
        Client client = parser.parse(userAgentString);

        UserAgent userAgent = client.userAgent;
        OS os = client.os;

        String browser = userAgent.family + " " + userAgent.major;
        String operatingSystem = os.family + " " + os.major;
        String deviceType = userAgentString.contains("Mobile") ? "Mobile" : "Desktop";
        TrustedDevice device = new TrustedDevice();
        device.setAddress(TOTPUtil.getClientIp(request));
        device.setOperatingSystem(operatingSystem);
        device.setBrowser(browser);
        device.setDeviceType(deviceType);
        device.setUserId(userId);
        device.setToken(token);
        device.setCreatedAt(Instant.now());
        device.setExpiresAt(Instant.now().plus(duration));
        repository.save(device);
        return token;
    }

    public void clearDevicesForUser(Long userId) {
        repository.deleteAllByUserId(userId);
    }

    public boolean isValidToken(String token, Long userId) {
        Optional<TrustedDevice> trusted = repository.findByTokenAndUserId(token, userId);
        if (trusted.isPresent()) {
            TrustedDevice trustedDevice = trusted.get();
            if (trustedDevice.getExpiresAt().isBefore(Instant.now())) {
                repository.delete(trustedDevice);
                return false;
            }
            return true;
        }
        return false;
    }

    public void removeToken(String token) {
        repository.deleteByToken(token);
    }

    public boolean isTrustedDevice(HttpServletRequest request, Long userId) {
        // Optional: check cookie/token to skip 2FA on trusted devices
        if (request.getCookies() == null) return false;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("trusted_device"))
                .map(Cookie::getValue)
                .anyMatch(value -> isValidToken(value, userId));
    }
}