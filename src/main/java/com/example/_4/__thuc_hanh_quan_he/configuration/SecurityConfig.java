package com.example._4.__thuc_hanh_quan_he.configuration;

import com.example._4.__thuc_hanh_quan_he.handler.CustomAuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    public SecurityConfig(CustomLoginSuccessHandler customLoginSuccessHandler) {
        this.customLoginSuccessHandler = customLoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Các tài nguyên tĩnh và trang công khai
                        .requestMatchers("/css/**", "/js/**", "/style/**", "/uploads/**","/forgot","/verify-otp","/reset-password").permitAll()
                        .requestMatchers("/", "/login", "/register", "/home", "/tagsController/genre/**").permitAll()
                        .requestMatchers("/user/chapters/**").authenticated()

                        .requestMatchers("/user/stories/**").hasAnyRole("USER", "ADMIN")
                        // Quyền cho Admin
                        .requestMatchers("/admin/**", "/authors/**", "/tagsController/**").hasRole("ADMIN")

                        // Quyền cho User (Link đăng truyện của Minh nằm ở đây)
                        .requestMatchers("/post-story/**", "/user/profile/**").hasAnyRole("USER", "ADMIN")

                        // Các API hoặc tính năng yêu cầu đăng nhập
                        .requestMatchers("/api/story/follow/**", "/comments/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler(new CustomAuthenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}