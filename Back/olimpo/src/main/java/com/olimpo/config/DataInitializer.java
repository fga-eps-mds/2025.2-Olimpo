package com.olimpo.config;

import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final List<String> PREDEFINED_KEYWORDS = List.of(
            "Tecnologia",
            "Saúde",
            "Educação",
            "Finanças",
            "Sustentabilidade",
            "Arte & Cultura",
            "E-commerce",
            "Impacto Social",
            "Indústria alimentícia",
            "Indústria Cinematográfica",
            "Geral",
            "Outros");

    @Bean
    CommandLineRunner initKeywords(KeywordRepository keywordRepository) {
        return args -> {
            PREDEFINED_KEYWORDS.forEach(name -> {
                if (keywordRepository.findByName(name).isEmpty()) {
                    keywordRepository.save(new Keyword(name));
                    System.out.println("Keyword cadastrada: " + name);
                }
            });
        };
    }
}