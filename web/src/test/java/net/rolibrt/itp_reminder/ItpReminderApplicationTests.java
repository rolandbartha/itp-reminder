package net.rolibrt.itp_reminder;

import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import net.rolibrt.itp_reminder.services.WebUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
class ItpReminderApplicationTests {
    private WebUserService userService;
    private WebUserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(WebUserRepository.class);
        userService = new WebUserService(userRepository);
    }

    @Test
    void testCreateAndGetUser() {
        WebUser user = new WebUser();
        user.setUsername("Bob");
        user.setEmail("bob@mail.com");
        Mockito.when(userRepository.findByEmail("bob@mail.com"))
                .thenReturn(Optional.of(user));

        WebUser result = userService.findByEmail("bob@mail.com").get();
        assertEquals("Bob", result.getUsername());
    }
}
