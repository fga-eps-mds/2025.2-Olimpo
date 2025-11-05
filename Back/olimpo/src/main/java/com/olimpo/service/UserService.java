package com.olimpo.service;

import com.olimpo.models.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.olimpo.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Account cadastrarUsuario(Account usuario){
        String senhaDigitada = usuario.getPassword();
        String senhaHasheada = passwordEncoder.encode(senhaDigitada);
        usuario.setPassword(senhaHasheada);
        return userRepository.save(usuario);
    }
}