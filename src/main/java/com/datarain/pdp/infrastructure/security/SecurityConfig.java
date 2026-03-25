package com.datarain.pdp.infrastructure.security;

import com.datarain.pdp.infrastructure.rate_limit.filter.RateLimitFilter;
import com.datarain.pdp.infrastructure.security.jwt.JwtAuthenticationFilter;
import com.datarain.pdp.infrastructure.security.web.RestAccessDeniedHandler;
import com.datarain.pdp.infrastructure.security.web.RestAuthenticationEntryPoint;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final TraceIdFilter traceIdFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitFilter rateLimitFilter,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler,
                          TraceIdFilter traceIdFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.traceIdFilter = traceIdFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // REST API هستیم
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Stateless چون JWT داریم
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 👇 این بخش کلیدی
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // مجوز دسترسی
                .authorizeHttpRequests(auth -> auth

                        // 🔓 Auth endpoints آزاد
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh").permitAll()


                        // 🔓 Actuator health برای liveness/readiness
                        .requestMatchers(EndpointRequest.to("health")).permitAll()
                        // 🔒 سایر actuator endpointها فقط برای ادمین
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority("ROLE_ADMIN")

                        // 🔓 Swagger آزاد
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 🔒 بقیه همه نیاز به توکن دارن
                        .anyRequest().authenticated()
                )

                // فیلترها
                .addFilterBefore(traceIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
