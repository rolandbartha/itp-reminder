package net.rolibrt.itp_reminder.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import net.rolibrt.itp_reminder.services.SettingsService;
import net.rolibrt.itp_reminder.services.TrustedDeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrafficFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(TrafficFilter.class);

    @Autowired
    private WebUserRepository userRepository;
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private AppVariables appVariables;
    @Autowired
    private TrustedDeviceService trustedDeviceService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (!appVariables.isProduction()) {
            String queryString = request.getQueryString();
            logger.info("Incoming Request: {} {}{}", method, path, (queryString != null ? "?" + queryString : ""));
        }
        if (path.startsWith("/api/")) {
            String apiKey = request.getHeader(settingsService.getString("api-header-name", "X-API-KEY"));
            if (!settingsService.getString("api-key", "your-secret-api-key").equals(apiKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API Key");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }
        if (isWhitelisted(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            WebUser user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                if (user.isClosed()) {
                    // Invalidate session and clear auth
                    request.getSession().invalidate();
                    SecurityContextHolder.clearContext();
                    // Redirect to closed account page
                    response.sendRedirect("/error/account-closed");
                    return;
                }
                if (user.isLocked()) {
                    if (user.isAccountLocked()) {
                        request.getSession().invalidate();
                        SecurityContextHolder.clearContext();
                        response.sendRedirect("/error/account-locked");
                        return;
                    } else {
                        userRepository.save(user);
                    }
                } else if (user.getFailedLoginAttempts() > 0) {
                    user.setFailedLoginAttempts(0);
                    userRepository.save(user);
                }
                if (user.isTwoFactorEnabled()) {
                    Boolean verified2FA = (Boolean) request.getSession().getAttribute("TWO_FACTOR_AUTHENTICATED");
                    if (Boolean.TRUE.equals(verified2FA) || trustedDeviceService.isTrustedDevice(request, user.getId())) {
                        request.getSession().setAttribute("TWO_FACTOR_AUTHENTICATED", true);
                    } else {
                        request.getSession().setAttribute("TWO_FACTOR_AUTHENTICATED", false);
                        response.sendRedirect("/2fa/enter-code");
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path, String method) {
        return path.startsWith("/login") ||
                path.startsWith("/logout") ||
                path.startsWith("/2fa") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                ("/error".equals(path) && "POST".equalsIgnoreCase(method));
    }
}
