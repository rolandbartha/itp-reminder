package net.rolibrt.itp_reminder.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
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
public class DataEntryCSV {
    @ExcelProperty("Id")
    private Long id;
    @ExcelProperty("Phone")
    private String phone;
    @ExcelProperty("Tag")
    private String tag;
    @ExcelProperty(value = "Date")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate date;
    @ExcelProperty("Duration")
    private Integer duration;
    @ExcelProperty("Creator")
    private String creator;
    @ExcelProperty("Reminded")
    private boolean reminded;

    public DataEntryCSV(DataEntry entry) {
        this(entry.getId(), entry.getPhone(), entry.getTag(), entry.getDate(), entry.getDuration(),
                entry.getCreatedBy() == null ? "" : entry.getCreatedBy().getUsername(), entry.isReminded());
    }
}
