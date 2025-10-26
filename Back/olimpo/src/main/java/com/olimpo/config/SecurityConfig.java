package com.olimpo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    @Bean // Define as regras de segurança HTTP
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilitar CSRF (Cross-Site Request Forgery) - Comum e necessário para APIs stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Define as regras de autorização para as requisições
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // Permite acesso PÚBLICO (sem autenticação) para POST no endpoint /user
                                .requestMatchers(HttpMethod.POST, "/user").permitAll()
                                // Permite acesso PÚBLICO para GET no endpoint /user/confirm (seu outro endpoint)
                                .requestMatchers(HttpMethod.GET, "/user/confirm").permitAll()
                                // Qualquer outra requisição PRECISA de autenticação
                                .anyRequest().authenticated()
                );

        return http.build();
    }
}
