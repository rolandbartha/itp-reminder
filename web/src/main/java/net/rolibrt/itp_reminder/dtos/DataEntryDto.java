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
public class DataEntryDto {
    private String phone;
    private String tag;
    private LocalDate date;
    private Integer duration = 12;
    private boolean reminded;

    public DataEntryDto(DataEntry entry) {
        this(entry.getPhone(), entry.getTag(), entry.getDate(), entry.getDuration(), entry.isReminded());
    }
}
