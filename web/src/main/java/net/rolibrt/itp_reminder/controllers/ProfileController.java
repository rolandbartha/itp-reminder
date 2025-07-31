package net.rolibrt.itp_reminder.controllers;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import net.rolibrt.itp_reminder.models.TrustedDevice;
import net.rolibrt.itp_reminder.dtos.TrustedDeviceDto;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.dtos.WebUserProfileDto;
import net.rolibrt.itp_reminder.repositories.TrustedDeviceRepository;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import net.rolibrt.itp_reminder.utils.TOTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    @Autowired
    private WebUserRepository userRepository;
    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;

    @GetMapping
    public String profile(HttpServletRequest request, Model model, Principal principal) {
        WebUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        List<TrustedDevice> trustedDevices = trustedDeviceRepository.findAllByUserId(user.getId());
        long currentDevice = -1;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("trusted_device")) {
                    currentDevice = trustedDevices.stream()
                            .filter(device -> device.getToken().equals(cookie.getValue()))
                            .mapToLong(TrustedDevice::getId)
                            .findAny().orElse(-1);
                    break;
                }
            }
        }

        model.addAttribute("user", new WebUserProfileDto(user));
        model.addAttribute("is2FA", user.isTwoFactorEnabled());
        model.addAttribute("devices", trustedDevices.stream().map(TrustedDeviceDto::new).toList());
        model.addAttribute("currentDevice", currentDevice);

        if (!user.isTwoFactorEnabled()) {
            if (user.getSecret() == null) {
                GoogleAuthenticatorKey key = TOTPUtil.generateSecretKey();
                user.setSecret(key.getKey());
                userRepository.save(user);
            }
            String otpAuthURL = TOTPUtil.getQRBarcodeURL(user.getUsername(), user.getSecret());
            String qrCodeBase64 = TOTPUtil.generateQRCodeImageBase64(otpAuthURL, 250, 250);
            model.addAttribute("qrCodeBase64", qrCodeBase64);
        }
        return "users/profile";
    }

    @PostMapping("/remove-device/{id}")
    public String remove(@PathVariable Long id, Principal principal) {
        WebUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Optional<TrustedDevice> optional = trustedDeviceRepository.findById(id);
        if (optional.isPresent()) {
            TrustedDevice device = optional.get();
            if (device.getUserId().equals(user.getId())) {
                trustedDeviceRepository.delete(device);
            }
        }
        return "redirect:/profile";
    }
}