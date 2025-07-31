package net.rolibrt.itp_reminder.components;

import net.rolibrt.itp_reminder.models.Setting;
import net.rolibrt.itp_reminder.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SettingsInitializer implements CommandLineRunner {

    private final SettingsRepository settingsRepository;

    @Autowired
    public SettingsInitializer(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, String> defaults = Map.of(
                "reminder_days", "10",
                "api-header-name", "X-API-KEY",
                "api-key", "your-secret-api-key",
                "trusted_device_time", "7" // days
        );
        if (settingsRepository.count() < defaults.size()) {
            List<String> settingKeys = settingsRepository.findAll()
                    .stream().map(Setting::getKey).toList();
            List<Setting> defaultSettings = defaults.entrySet().stream()
                    .filter(entry -> !settingKeys.contains(entry.getKey()))
                    .map(entry -> new Setting(entry.getKey(), entry.getValue()))
                    .toList();
            settingsRepository.saveAll(defaultSettings);
            System.out.println("Loaded " + defaultSettings.size() + " default settings.");
        }
    }
}