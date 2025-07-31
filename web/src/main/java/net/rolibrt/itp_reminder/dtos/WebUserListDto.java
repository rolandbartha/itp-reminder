package net.rolibrt.itp_reminder.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.WebUser;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebUserListDto {
    private Long id;
    private String username;
    private String email;
    private String roles;
    private boolean closed, locked;

    public WebUserListDto(WebUser user) {
        this(user.getId(), user.getUsername(), user.getEmail(), user.getRoleList(), user.isClosed(), user.isLocked());
    }
}
