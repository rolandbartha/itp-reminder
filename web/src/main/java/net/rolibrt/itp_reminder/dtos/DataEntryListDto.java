package net.rolibrt.itp_reminder.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.DataEntry;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataEntryListDto {
    private Long id;
    private String phone;
    private String tag;
    private LocalDate date;
    private LocalDate expiry;
    private String creator;
    private boolean reminded, expired, soon;

    public DataEntryListDto(DataEntry entry, int reminderDays) {
        this(entry.getId(), entry.getPhone(), entry.getTag(), entry.getDate(), entry.getExpiry(),
                entry.getCreatedBy() == null ? "" : entry.getCreatedBy().getUsername(),
                entry.isReminded(), entry.isExpired(), entry.isSoon(reminderDays));
    }
}
