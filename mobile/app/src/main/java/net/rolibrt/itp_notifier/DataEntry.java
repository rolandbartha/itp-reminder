package net.rolibrt.itp_notifier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataEntry {
    private Long id;
    private String phone;
    private String tag;
    private LocalDate date, expiry;
    private String creator;
    private boolean reminded;
    private boolean expired;
    private boolean selected;
}