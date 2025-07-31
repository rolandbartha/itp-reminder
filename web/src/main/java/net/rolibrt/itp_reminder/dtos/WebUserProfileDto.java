package net.rolibrt.itp_reminder.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.Role;
import net.rolibrt.itp_reminder.models.WebUser;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebUserProfileDto {

    private String username, email;
    private List<Role> roles;

    public WebUserProfileDto(WebUser user) {
        this(user.getUsername(), user.getEmail(), user.getRoles());
    }
}
