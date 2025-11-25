package com.olimpo.config;

import com.olimpo.repository.UserRepository;
import com.olimpo.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilter securityFilter() {
        return new SecurityFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public TokenService tokenService() {
        return Mockito.mock(TokenService.class);
    }

    @Bean
    public UserRepository testUserRepository() {
        return Mockito.mock(UserRepository.class);
    }
}
