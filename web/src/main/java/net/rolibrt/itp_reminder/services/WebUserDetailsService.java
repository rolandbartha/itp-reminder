package net.rolibrt.itp_reminder.services;

import net.rolibrt.itp_reminder.models.Role;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebUserDetailsService implements UserDetailsService {

    @Autowired
    private WebUserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<WebUser> optionalUser = repository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return User.builder()
                    .username("unknown")
                    .password("{noop}invalid") // Or a bcrypt-encoded fake password
                    .roles("USER") // Fake role
                    .accountLocked(true) // Prevent access
                    .build();
        }

        WebUser user = optionalUser.get();
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::toString).toArray(String[]::new))
                .accountLocked(user.isLocked())
                .disabled(user.isClosed())
                .build();
    }
}