package net.rolibrt.itp_reminder.components;

import net.rolibrt.itp_reminder.models.Role;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);
    private final WebUserRepository webUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializer(WebUserRepository webUserRepository, BCryptPasswordEncoder passwordEncoder) {
        this.webUserRepository = webUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createAdmin() {
        if (webUserRepository.count() == 0) {
            WebUser admin = new WebUser();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(List.of(Role.values()));
            logger.info("Admin user created: admin / admin123");
            webUserRepository.save(admin);
        }
    }
}