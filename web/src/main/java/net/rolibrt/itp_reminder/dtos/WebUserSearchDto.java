package net.rolibrt.itp_reminder.dtos;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.Role;
import net.rolibrt.itp_reminder.models.WebUser;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebUserSearchDto {
    private String username, email, sortBy = "id", direction = "asc";
    private Role role = null;
    private Boolean closed, locked;
    private int page = 0, size = 50;

    public static Specification<WebUser> filter(WebUserSearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + dto.getUsername().toLowerCase() + "%"));
            }
            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + dto.getEmail().toLowerCase() + "%"));
            }
            if (dto.getRole() != null) {
                predicates.add(cb.like(cb.lower(root.get("roles").as(String.class)),
                        "%" + dto.getRole().toString().toLowerCase() + "%"));
            }
            if (dto.getClosed() != null) {
                predicates.add(cb.equal(root.get("closed"), dto.getClosed()));
            }
            if (dto.getLocked() != null) {
                predicates.add(cb.equal(root.get("locked"), dto.getLocked()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
