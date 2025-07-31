package net.rolibrt.itp_reminder.repositories;

import net.rolibrt.itp_reminder.models.WebUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebUserRepository extends JpaRepository<WebUser, Long>
        , JpaSpecificationExecutor<WebUser> {
    Optional<WebUser> findByUsername(String username);

    Optional<WebUser> findByEmail(String email);
}