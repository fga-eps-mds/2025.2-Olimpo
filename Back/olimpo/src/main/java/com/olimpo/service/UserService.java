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
        if(userRepository.findByEmail(usuario.getEmail()).isPresent()){
            /*
            Se o Optional retornado da função findByEmail não estiver vazio o isPresente será igual a true
            E isso significa que já existe uma conta com este e-mail e é preciso lançar uma exception para
            parar a execução
             */
            throw new RuntimeException("E-mail já cadastrado");
        }

        String senhaDigitada = usuario.getPassword();
        String senhaHasheada = passwordEncoder.encode(senhaDigitada);
        usuario.setPassword(senhaHasheada);
        return userRepository.save(usuario);
    }
}
