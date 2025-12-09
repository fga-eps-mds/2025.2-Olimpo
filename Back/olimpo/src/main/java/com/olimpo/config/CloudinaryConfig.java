package com.olimpo.config;

import com.cloudinary.Cloudinary;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${CLOUDINARY_CLOUD_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();

        String cleanCloudName = stripQuotes(cloudName.trim());
        String cleanApiKey = stripQuotes(apiKey.trim());
        String cleanApiSecret = stripQuotes(apiSecret.trim());

        System.out.println("========================================");
        System.out.println("CONFIGURAÇÃO CLOUDINARY CARREGADA:");
        System.out.println("Cloud Name: '" + cleanCloudName + "'");
        System.out.println("API Key:    '" + cleanApiKey + "'");
        // Não imprima o segredo inteiro por segurança
        System.out.println(
                "API Secret: '" + (cleanApiSecret != null ? cleanApiSecret.substring(0, 3) + "..." : "null") + "'");
        System.out.println("Secret Length: " + (cleanApiSecret != null ? cleanApiSecret.length() : "null"));
        System.out.println("========================================");

        config.put("cloud_name", cleanCloudName);
        config.put("api_key", cleanApiKey);
        config.put("api_secret", cleanApiSecret);
        config.put("secure", "true");

        return new Cloudinary(config);
    }

    private String stripQuotes(String value) {
        if (value == null)
            return null;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}