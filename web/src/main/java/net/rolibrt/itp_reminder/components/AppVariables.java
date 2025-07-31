package net.rolibrt.itp_reminder.components;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppVariables {
    @Value("${spring.prod}")
    private boolean production;
    @Value("${security.remember-me.key}")
    private String rememberMeKey;
}