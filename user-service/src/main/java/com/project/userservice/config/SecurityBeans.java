package com.project.userservice.config;

import com.project.userservice.utils.JwtTokenConverter;
import jakarta.annotation.Priority;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    @Priority(0)
    public SecurityFilterChain metricsFilterChain(HttpSecurity http) throws Exception {
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
    @Priority(1)
    public SecurityFilterChain gatewaySecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/actuator/**").hasAuthority("SCOPE_metrics")
                        .requestMatchers("/users/register", "/users/changePassword", "/users/login", "/users/grantCode").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/refreshToken", "/users/resetPassword").permitAll()
                        .requestMatchers("/users/getUserIdByToken").hasAuthority("SCOPE_view_users")
                        .requestMatchers("/users/checkUserExists").hasAuthority("SCOPE_view_users")
                        .requestMatchers("/users/dashboard").authenticated()
                        .requestMatchers("/v2/api-docs","/v3/api-docs","/v3/api-docs/**","/swagger-resources","/swagger-resources/**",
                                "/configuration/ui","/configuration/security","/swagger-ui/**","/webjars/**","/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtTokenConverter)))
                .logout(logout -> logout.logoutSuccessUrl("/users/login"))
//                .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> response.sendRedirect("http://localhost:8080/users/login")))
                .build();
    }

}
