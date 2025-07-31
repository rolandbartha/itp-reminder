package net.rolibrt.itp_reminder.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Setting {

    @Id
    @Column(name = "`key`", nullable = false)
    private String key;

    private String value;

    public int getValueAsInt() {
        return Integer.parseInt(value);
    }

    public boolean keysMatch(Setting other) {
        return key.equals(other.key);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Setting other) {
            return key.equals(other.key) && value.equals(other.value);
        }
        return false;
    }
}
