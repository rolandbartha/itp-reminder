package net.rolibrt.itp_reminder.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.*;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventsLogger
        implements ApplicationListener<AbstractAuthenticationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventsLogger.class);

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        String username = event.getAuthentication().getName();
        if (event instanceof AuthenticationSuccessEvent) {
            logger.info("LOGIN SUCCESS for user: {}", username);
        } else if (event instanceof AbstractAuthenticationFailureEvent) {
            logger.info("LOGIN FAILURE for user: {}", username);
        } else if (event instanceof LogoutSuccessEvent) {
            logger.info("LOGOUT SUCCESS for user: {}", username);
        }
    }
}
