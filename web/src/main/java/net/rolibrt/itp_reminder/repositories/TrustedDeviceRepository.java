package net.rolibrt.itp_reminder.repositories;

import jakarta.transaction.Transactional;
import net.rolibrt.itp_reminder.models.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {
    List<TrustedDevice> findAllByUserId(Long userId);

    Optional<TrustedDevice> findByTokenAndUserId(String token, Long userId);

    @Transactional
    void deleteByToken(String token);

    void deleteAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TrustedDevice td WHERE td.expiresAt < :now")
    void deleteAllExpired(@Param("now") Instant now);
}