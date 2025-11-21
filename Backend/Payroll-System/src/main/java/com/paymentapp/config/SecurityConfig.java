package com.paymentapp.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.paymentapp.security.JwtAuthenticationEntryPoint;
import com.paymentapp.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/users/login",
                        "/api/auth/login",
                        "/api/auth/forgot-password",     
                        "/api/auth/verify-otp",          
                        "/api/auth/reset-password", 
                        "/api/organizations/register",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                ).permitAll()
                
                .requestMatchers("/api/notifications/**").authenticated()

                .requestMatchers("/api/salary-disbursal/approve-or-reject").hasAuthority("ROLE_BANK_ADMIN")
                .requestMatchers("/api/salary-disbursal/**").hasAuthority("ROLE_ORGANIZATION")
                
                .requestMatchers("/api/bank-admin/**").hasAuthority("ROLE_BANK_ADMIN") 
                .requestMatchers("/api/organizations/**").hasAnyAuthority("ROLE_BANK_ADMIN","ROLE_ORGANIZATION", "ROLE_ORG_ADMIN")
                .requestMatchers("/api/orgadmins/**").hasAnyAuthority("ROLE_ORGANIZATION")
                .requestMatchers("/api/payments/**").hasAnyAuthority("ROLE_ORGANIZATION")
                .requestMatchers("/api/employees/**").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ORG_ADMIN", "ROLE_ORGANIZATION")
                .requestMatchers("/api/vendors/**").hasAnyAuthority("ROLE_VENDOR", "ROLE_ORG_ADMIN", "ROLE_ORGANIZATION")
                .requestMatchers("/api/salary-grades/**").hasAnyAuthority("ROLE_ORGANIZATION")
                .requestMatchers("/api/salary-slip").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_ORG_ADMIN", "ROLE_ORGANIZATION")
                .requestMatchers("/api/payment-requests").hasAuthority("ROLE_ORGANIZATION")
                .requestMatchers("/api/payment-requests/approve").hasAuthority("ROLE_BANK_ADMIN")
                .requestMatchers("/api/payment-receipts/**").hasAnyAuthority("ROLE_ORGANIZATION", "ROLE_VENDOR")

                .anyRequest().authenticated()
            );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200", 
            "https://localhost:4200"  
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
