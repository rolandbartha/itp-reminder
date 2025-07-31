package net.rolibrt.itp_reminder.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.rolibrt.itp_reminder.components.AppVariables;
import net.rolibrt.itp_reminder.components.TwoFactorCache;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import net.rolibrt.itp_reminder.services.SettingsService;
import net.rolibrt.itp_reminder.services.TrustedDeviceService;
import net.rolibrt.itp_reminder.utils.TOTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Controller
@RequestMapping("/2fa")
public class TOTPController {
    private static final Logger logger = LoggerFactory.getLogger(TOTPController.class);
    @Autowired
    private WebUserRepository userRepository;
    @Autowired
    private TwoFactorCache twoFactorCache;
    @Autowired
    private TrustedDeviceService trustedDeviceService;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AppVariables appVariables;

    @GetMapping("/enter-code")
    public String showVerifyPage(Model model) {
        model.addAttribute("trustedDays", settingsService.getInt("trusted_device_time", 7));
        return "2fa/enter-code";
    }

    @PostMapping("/enter-code")
    public String verify(@RequestParam("code") String code,
                         @RequestParam(required = false) boolean trustDevice,
                         Principal principal,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated.");
            return "redirect:/login";
        }
        Optional<WebUser> optionalUser = userRepository.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(code); // Or @RequestParam
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid code format.");
            return "redirect:/2fa/enter-code";
        }
        WebUser user = optionalUser.get();
        if (twoFactorCache.isCodeUsed(user.getUsername(), verificationCode)) {
            redirectAttributes.addFlashAttribute("error", "This code has already been used.");
            return "redirect:/2fa/enter-code";
        }
        boolean valid = TOTPUtil.isCodeValid(user.getSecret(), verificationCode);
        if (valid) {
            if (user.getFailed2FAAttempts() > 0) {
                user.setFailed2FAAttempts(0);
                userRepository.save(user);
            }
            twoFactorCache.markCodeUsed(user.getUsername(), verificationCode);
            request.getSession().setAttribute("TWO_FACTOR_AUTHENTICATED", true);
            if (trustDevice) {
                Duration duration = Duration.ofDays(settingsService.getInt("trusted_device_time", 7));
                String token = trustedDeviceService.createToken(request, user.getId(), duration);
                ResponseCookie cookie = ResponseCookie.from("trusted_device", token)
                        .httpOnly(true)
                        .secure(appVariables.isProduction())
                        .path("/")
                        .maxAge(duration)
                        .sameSite("Strict")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            }
            return "redirect:/home";
        } else {
            int fails = user.getFailed2FAAttempts() + 1;
            user.setFailed2FAAttempts(fails);
            if (fails >= 5) {
                user.setLocked(true);
                user.setLockTime(Instant.now());
                redirectAttributes.addFlashAttribute("error", "Too many failed attempts. Account locked.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid authentication code.");
            }
            userRepository.save(user);
            return "redirect:/2fa/enter-code";
        }
    }

    @PostMapping("/enable")
    public String enable(@RequestParam("code") String code,
                         Principal principal,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated.");
            return "redirect:/login";
        }
        Optional<WebUser> optionalUser = userRepository.findByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(code); // Or @RequestParam
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid code format.");
            return "redirect:/profile";
        }
        WebUser user = optionalUser.get();
        if (twoFactorCache.isCodeUsed(user.getUsername(), verificationCode)) {
            redirectAttributes.addFlashAttribute("error", "This code has already been used.");
            return "redirect:/profile";
        }
        logger.info("Enabling 2FA for user: {} with code: {}", user.getUsername(), verificationCode);

        boolean valid = TOTPUtil.isCodeValid(user.getSecret(), verificationCode);
        logger.info("Is code valid? {}", valid);
        if (valid) {
            user.setTwoFactorEnabled(true);
            user.setFailed2FAAttempts(0);
            twoFactorCache.markCodeUsed(user.getUsername(), verificationCode);
            request.getSession().setAttribute("TWO_FACTOR_AUTHENTICATED", true);
            redirectAttributes.addFlashAttribute("success", "2FA enabled!");
        } else {
            int fails = user.getFailed2FAAttempts() + 1;
            user.setFailed2FAAttempts(fails);
            if (fails >= 5) {
                user.setLocked(true);
                user.setLockTime(Instant.now());
                redirectAttributes.addFlashAttribute("error", "Too many failed attempts. Account locked.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid authentication code.");
            }
        }
        userRepository.save(user);
        return "redirect:/profile";
    }

    @PostMapping("/disable")
    public String disable(@RequestParam("code") String code,
                          Principal principal,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttributes) {
        if (principal == null || principal.getName() == null) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated.");
            return "redirect:/login";
        }

        Optional<WebUser> optionalUser = userRepository.findByUsername(principal.getName());

        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }
        int verificationCode;
        try {
            verificationCode = Integer.parseInt(code); // Or @RequestParam
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid code format.");
            return "redirect:/profile";
        }

        WebUser user = optionalUser.get();
        if (twoFactorCache.isCodeUsed(user.getUsername(), verificationCode)) {
            redirectAttributes.addFlashAttribute("error", "This code has already been used.");
            return "redirect:/profile";
        }
        logger.info("Disabling 2FA for user: {} with code: {}", user.getUsername(), verificationCode);

        boolean valid = TOTPUtil.isCodeValid(user.getSecret(), verificationCode);
        logger.info("Is code valid? {}", valid);
        if (valid) {
            twoFactorCache.markCodeUsed(user.getUsername(), verificationCode);
            trustedDeviceService.clearDevicesForUser(user.getId());
            request.getSession().removeAttribute("TWO_FACTOR_AUTHENTICATED");
            user.setFailed2FAAttempts(0);
            user.setTwoFactorEnabled(false);
            user.setSecret(null);
            redirectAttributes.addFlashAttribute("success", "2FA disabled.");
        } else {
            int fails = user.getFailed2FAAttempts() + 1;
            user.setFailed2FAAttempts(fails);
            if (fails >= 5) {
                user.setLocked(true);
                user.setLockTime(Instant.now());
                redirectAttributes.addFlashAttribute("error", "Too many failed attempts. Account locked.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid authentication code.");
            }
        }
        userRepository.save(user);
        return "redirect:/profile";
    }


}
