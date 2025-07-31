package net.rolibrt.itp_reminder.services;

import net.rolibrt.itp_reminder.models.Setting;
import net.rolibrt.itp_reminder.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private final SettingsRepository repository;

    @Autowired
    public SettingsService(SettingsRepository repository) {
        this.repository = repository;
    }

    public int getInt(String key, int value) {
        return repository.findById(key)
                .map(Setting::getValueAsInt)
                .orElse(value);
    }

    public Setting get(String key, Setting defaultSetting) {
        return repository.findById(key)
                .orElse(defaultSetting);
    }

    public String getString(String key, String defaultSetting) {
        return repository.findById(key)
                .orElse(new Setting(key, defaultSetting))
                .getValue();
    }

    public void save(Setting setting) {
        repository.save(setting);
    }

    public List<Setting> getAllSettings() {
        return repository.findAll();
    }

    public void saveAll(List<Setting> settings) {
        repository.saveAll(settings);
    }

    public void updateSettings(Map<String, String> settingsMap) {
        settingsMap.forEach((key, value) -> {
            if (!key.equals("_csrf")) {
                save(new Setting(key, value));
            }
        });
    }
}