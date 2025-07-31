package net.rolibrt.itp_reminder.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.Role;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebUserDto {

    @NotBlank
    private String username;

    private String password;

    private String email;

    private boolean closed, locked;

    private List<Role> roles;
}
