package net.rolibrt.itp_reminder.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.utils.RoleSetConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "web_user")
public class WebUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true)
    private String email;
    private String password;
    @Convert(converter = RoleSetConverter.class)
    private List<Role> roles = new ArrayList<>();
    private LocalDate createdAt;
    private LocalDate lastLogin;
    private boolean closed, locked;
    private int failedLoginAttempts = 0;
    private Instant lockTime;

    private boolean twoFactorEnabled;
    private String secret;
    private int failed2FAAttempts;

    public String getRoleList() {
        return roles.stream().map(Role::toString).collect(Collectors.joining(","));
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString()))
                .toList();
    }

    public boolean isAccountLocked() {
        if (locked) {
            if (lockTime != null && lockTime.plus(15, ChronoUnit.MINUTES).isBefore(Instant.now())) {
                locked = false;
                failedLoginAttempts = 0;
                lockTime = null;
                return false;
            }
            return true;
        }
        return false;
    }
}