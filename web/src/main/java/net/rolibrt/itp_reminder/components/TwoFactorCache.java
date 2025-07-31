package net.rolibrt.itp_reminder.components;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TwoFactorCache {

    // Cache username -> last used TOTP code
    private final Cache<String, Integer> usedCodes = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES) // keep codes for 2 mins (TOTP codes expire in 30s typically)
            .build();

    public boolean isCodeUsed(String username, int code) {
        Integer lastCode = usedCodes.getIfPresent(username);
        return lastCode != null && code == lastCode;
    }

    public void markCodeUsed(String username, int code) {
        usedCodes.put(username, code);
    }

    public void clearUser(String username) {
        usedCodes.invalidate(username);
    }
}

