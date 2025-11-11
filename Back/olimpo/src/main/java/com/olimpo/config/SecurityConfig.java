package com.olimpo.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) 
                
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Permitir Cadastro
                                .requestMatchers(HttpMethod.POST, "/user").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/user").permitAll() 
                                
                                // Permitir Login
                                .requestMatchers(HttpMethod.POST, "/user/login").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/user/login").permitAll() 

                                // Permitir Verificação de Email (NOVO)
                                .requestMatchers(HttpMethod.GET, "/user/verify-email").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/user/verify-email").permitAll()

                                // Permitir Reenvio de Código (NOVO)
                                .requestMatchers(HttpMethod.POST, "/user/resend-code").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/user/resend-code").permitAll()
                                
                                // REMOVIDO: .requestMatchers(HttpMethod.GET, "/user/confirm").permitAll()
                                
                                // Permitir Recuperação de Senha
                                .requestMatchers("/api/password/**").permitAll()
                                
                                .anyRequest().authenticated()
                );

        return http.build();
    }
}