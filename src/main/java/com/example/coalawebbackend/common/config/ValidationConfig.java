package com.example.coalawebbackend.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class ValidationConfig {
    @Bean
    public Clock businessClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }

    @Bean
    public LocalValidatorFactoryBean defaultValidator(Clock businessClock) {
        var validator = new LocalValidatorFactoryBean();
        // Date-only validation follows the club's calendar, not the host's timezone.
        validator.setConfigurationInitializer(config -> config.clockProvider(() -> businessClock));
        return validator;
    }
}
