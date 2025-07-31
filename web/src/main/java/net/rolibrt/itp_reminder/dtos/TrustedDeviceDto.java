package net.rolibrt.itp_reminder.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.rolibrt.itp_reminder.models.TrustedDevice;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class TrustedDeviceDto {
    private Long id;
    private String address, operatingSystem, browser, deviceType;
    private Instant createdAt, expiresAt;

    public TrustedDeviceDto(TrustedDevice device) {
        this(device.getId(), device.getAddress(), device.getOperatingSystem(), device.getBrowser(),
                device.getDeviceType(), device.getCreatedAt(), device.getExpiresAt());
    }
}