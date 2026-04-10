package com.datarain.pdp.config;

import com.datarain.pdp.infrastructure.security.lockout.LockoutProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LockoutProperties.class)
public class SecurityPropertiesConfig {
}
