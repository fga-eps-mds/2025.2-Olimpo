package com.olimpo.config;

import com.olimpo.repository.UserRepository;
import com.olimpo.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("--- [SecurityFilter] Iniciando filtro para: " + request.getRequestURI() + " ---");

        var token = this.recoverToken(request);

        if(token != null){
            System.out.println("[SecurityFilter] Token encontrado: " + token.substring(0, Math.min(token.length(), 10)) + "...");

            var login = tokenService.validateToken(token);
            System.out.println("[SecurityFilter] Resultado da validação (email): " + login);

            if(login != null && !login.isEmpty()){
                UserDetails user = userRepository.findByEmail(login).orElse(null);

                if (user != null) {
                    System.out.println("[SecurityFilter] Usuário encontrado no banco: " + user.getUsername());
                    System.out.println("[SecurityFilter] Autoridades: " + user.getAuthorities());

                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("[SecurityFilter] Autenticação definida no contexto!");
                } else {
                    System.out.println("[SecurityFilter] ERRO: Usuário não encontrado no banco para o email: " + login);
                }
            } else {
                System.out.println("[SecurityFilter] ERRO: Token inválido ou expirado (validateToken retornou vazio).");
            }
        } else {
            System.out.println("[SecurityFilter] Nenhum token encontrado no cabeçalho.");
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        // Remove espaços extras apenas por segurança
        return authHeader.replace("Bearer ", "").trim();
    }
}