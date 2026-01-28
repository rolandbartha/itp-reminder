package net.rolibrt.itp_reminder.repositories;

import net.rolibrt.itp_reminder.models.DataEntry;
import net.rolibrt.itp_reminder.models.WebUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DataEntryRepository extends JpaRepository<DataEntry, Long>, JpaSpecificationExecutor<DataEntry> {
    List<DataEntry> findByCreatedBy(WebUser user);

    List<DataEntry> findByPhone(String phone);

    Optional<DataEntry> findByTag(String tag);

    @Query(value = "SELECT * FROM data_entry WHERE reminded = false AND " +
            "DATE_SUB(DATE_ADD(date, INTERVAL duration MONTH), INTERVAL :reminderDays DAY) <= CURDATE()",
            nativeQuery = true)
    List<DataEntry> findDue(@Param("reminderDays") int reminderDays);

    @Modifying
    @Query("UPDATE DataEntry d SET d.reminded = :newValue WHERE d.id IN :ids")
    int updateRemindedByIds(@Param("newValue") boolean newValue, @Param("ids") Set<Long> ids);
}