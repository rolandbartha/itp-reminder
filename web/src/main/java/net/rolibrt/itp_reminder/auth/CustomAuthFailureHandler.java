package net.rolibrt.itp_reminder.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthFailureHandler.class);

    @Autowired
    private WebUserRepository webUserRepository;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {
        String username = request.getParameter("username"); // get username from login form
        if (username != null) {
            webUserRepository.findByUsername(username).ifPresent(user -> {
                int newFailAttempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(newFailAttempts);
                if (newFailAttempts >= 5) {
                    user.setLocked(true);
                    user.setLockTime(Instant.now());
                    logger.warn("Locked {}'s account due to too many failed login attempts!", user.getUsername());
                }
                webUserRepository.save(user);
            });
        }

        // Decide redirect URL based on exception type
        String error = "bad_credentials";
        if (exception instanceof LockedException) {
            error = "locked";
        } else if (exception instanceof DisabledException) {
            error = "closed";
        }
        response.sendRedirect("/login?error=" + error);
    }
}

