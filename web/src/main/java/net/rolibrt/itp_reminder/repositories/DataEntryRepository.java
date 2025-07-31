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

    @Query("SELECT e FROM DataEntry e WHERE e.reminded = false AND e.date <= :targetDate")
    List<DataEntry> findDue(@Param("targetDate") LocalDate targetDate);

    @Modifying
    @Query("UPDATE DataEntry d SET d.reminded = :newValue WHERE d.id IN :ids")
    int updateRemindedByIds(@Param("newValue") boolean newValue, @Param("ids") Set<Long> ids);
}