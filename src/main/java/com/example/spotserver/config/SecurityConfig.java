package com.example.spotserver.config;

import com.example.spotserver.filter.MyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private CorsFilter corsFilter;

    @Autowired
    public SecurityConfig(CorsFilter corsFilter) {
        this.corsFilter = corsFilter;
    }

    // 해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session ->
                session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // 세션 생성 X

        http.authorizeHttpRequests(request ->
                request
                        .requestMatchers("/user/**").authenticated() // 인증만 된다면 들어갈 수 있는 주소
                        .requestMatchers("/manager/**").hasAnyAuthority("admin", "manager")
                        .requestMatchers("/admin/**").hasAuthority("admin")
                        .anyRequest().permitAll());

        http.formLogin(formLogin ->
                formLogin
                        .disable()); // 폼 태그 로그인 안쓰겠다.

        http.httpBasic(httpBasic ->
                httpBasic
                        .disable()); // 기본적인 HTTP 로그인 안쓰겠다. (ID, PW를 항상 포함하여 요청함)

        http.addFilter(corsFilter);
        return http.build();
    }

}
