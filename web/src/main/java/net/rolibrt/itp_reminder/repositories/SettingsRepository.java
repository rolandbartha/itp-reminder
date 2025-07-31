package net.rolibrt.itp_reminder.repositories;

import net.rolibrt.itp_reminder.models.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<Setting, String> {
}