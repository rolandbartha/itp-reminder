package net.rolibrt.itp_reminder.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.dtos.DataEntryCSV;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "data_entry")
public class DataEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;
    @Column(unique = true)
    private String tag;
    private LocalDate date;
    private Integer duration = 12;
    private boolean reminded;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private WebUser createdBy;

    @PostLoad
    @PostPersist
    public void applyDefaultValues() {
        if (duration == null) {
            duration = 12;
        }
    }

    public LocalDate getExpiry() {
        return this.date.plusMonths(duration);
    }

    public boolean isExpired() {
        return getExpiry().isBefore(LocalDate.now());
    }

    public boolean isSoon(int days) {
        return !isExpired() && this.date.isBefore(LocalDate.now()
                .minusMonths(duration)
                .plusDays(days));
    }

    public boolean importCSV(DataEntryCSV csv) {
        phone = csv.getPhone();
        tag = csv.getTag();
        date = csv.getDate();
        duration = csv.getDuration();
        reminded = csv.isReminded();
        boolean noCreator = createdBy == null;
        boolean isNewBlank = csv.getCreator() == null || csv.getCreator().isBlank();
        return noCreator != isNewBlank || !noCreator && !createdBy.getUsername().equals(csv.getCreator());
    }
}
