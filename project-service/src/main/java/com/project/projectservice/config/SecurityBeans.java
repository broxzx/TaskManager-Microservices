package com.project.projectservice.config;

import com.project.projectservice.utils.JwtTokenConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityBeans {

    private final JwtTokenConverter jwtTokenConverter;

    @Bean
    @Order(0)
    public SecurityFilterChain metricsSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/**")
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .anyRequest().denyAll())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(Customizer.withDefaults()))
                .build();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/projects/**")
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtTokenConverter)))
                .build();
    }

}
