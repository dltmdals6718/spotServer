package com.example.spotserver;

import com.example.spotserver.config.jwt.JwtAccessDenyHandler;
import com.example.spotserver.config.jwt.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session ->
                session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 생성 X

        http.authorizeHttpRequests(request ->
                request
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/members").authenticated()
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .requestMatchers(HttpMethod.POST, "/members/signup", "/members/signin").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated());

        http.formLogin(formLogin ->
                formLogin
                        .disable()); // 폼 태그 로그인 안쓰겠다.

        http.httpBasic(httpBasic ->
                httpBasic
                        .disable()); // 기본적인 HTTP 로그인 안쓰겠다. (ID, PW를 항상 포함하여 요청함)

        http.exceptionHandling(e -> e
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDenyHandler()));

        return http.build();
    }
}
