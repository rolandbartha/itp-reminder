package net.rolibrt.itp_reminder.components;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class LogFolderInitializer {

    @PostConstruct
    public void initLogDir() {
        new File("logs").mkdirs();
    }
}
