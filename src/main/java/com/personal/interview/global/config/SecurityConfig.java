package com.personal.interview.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.personal.interview.global.config.properties.AuthProperties;
import com.personal.interview.global.config.properties.JwtProperties;
import com.personal.interview.global.security.JwtAuthenticationFilter;
import com.personal.interview.global.security.JwtTokenProvider;
import com.personal.interview.global.security.filter.CustomAuthenticationFilter;
import com.personal.interview.global.security.handler.JwtAccessDeniedHandler;
import com.personal.interview.global.security.handler.JwtAuthenticationEntryPoint;
import com.personal.interview.global.security.handler.JwtAuthenticationFailureHandler;
import com.personal.interview.global.security.handler.JwtAuthenticationSuccessHandler;
import com.personal.interview.global.security.service.RefreshTokenService;
import com.personal.interview.util.validator.SecurityValidatorUtil;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@EnableConfigurationProperties({ AuthProperties.class, JwtProperties.class })
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final SecurityValidatorUtil securityValidatorUtil;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(AuthenticationManager authenticationManager) {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(authenticationManager, objectMapper,
                securityValidatorUtil);
        filter.setAuthenticationSuccessHandler(
                new JwtAuthenticationSuccessHandler(jwtTokenProvider, objectMapper, refreshTokenService));
        filter.setAuthenticationFailureHandler(
                new JwtAuthenticationFailureHandler(objectMapper));
        return filter;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CustomAuthenticationFilter customAuthenticationFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/refresh").permitAll()
                        .requestMatchers("/api/auth/email/verify").permitAll()
                        .requestMatchers("/api/auth/email/verify/redirect").permitAll()
                        .requestMatchers("/api/user/signup").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
