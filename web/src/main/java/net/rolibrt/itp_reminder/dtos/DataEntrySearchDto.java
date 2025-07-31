package net.rolibrt.itp_reminder.dtos;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.DataEntry;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataEntrySearchDto {
    private String phone, tag, sortBy = "id", direction = "asc";
    private LocalDate fromDate, toDate;
    private Boolean reminded;
    private int page = 0, size = 50;

    public static Specification<DataEntry> filter(DataEntrySearchDto dto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("phone")), "%" + dto.getPhone().toLowerCase() + "%"));
            }
            if (dto.getTag() != null && !dto.getTag().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tag")), "%" + dto.getTag().toLowerCase() + "%"));
            }
            if (dto.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), dto.getFromDate()));
            }
            if (dto.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), dto.getToDate()));
            }
            if (dto.getReminded() != null) {
                predicates.add(cb.equal(root.get("reminded"), dto.getReminded()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
