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

    @Value("${CLOUDINARY_CLOUD_NAME:}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY:}")
    private String apiKey;

    @Value("${CLOUDINARY_API_SECRET:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();

        String cleanCloudName = normalize(cloudName);
        String cleanApiKey = normalize(apiKey);
        String cleanApiSecret = normalize(apiSecret);

        validateCredentials(cleanCloudName, cleanApiKey, cleanApiSecret);
        logConfiguration(cleanCloudName, cleanApiKey, cleanApiSecret);

        config.put("cloud_name", cleanCloudName);
        config.put("api_key", cleanApiKey);
        config.put("api_secret", cleanApiSecret);
        config.put("secure", "true");

        return new Cloudinary(config);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("=", "").trim();
    }

    private void validateCredentials(String cleanCloudName, String cleanApiKey, String cleanApiSecret) {
        if (cleanCloudName.isBlank() || cleanApiKey.isBlank() || cleanApiSecret.isBlank()) {
            throw new IllegalStateException("Cloudinary não configurado: defina CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY e CLOUDINARY_API_SECRET");
        }
    }

    private void logConfiguration(String cleanCloudName, String cleanApiKey, String cleanApiSecret) {
        System.out.println("========================================");
        System.out.println("CONFIGURAÇÃO CLOUDINARY CARREGADA:");
        System.out.println("Cloud Name: '" + cleanCloudName + "'");
        System.out.println("API Key:    '" + cleanApiKey + "'");
        System.out.println("API Secret: '" + maskSecret(cleanApiSecret) + "'");
        System.out.println("========================================");
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return "(vazio)";
        }
        return secret.length() <= 3 ? "***" : secret.substring(0, 3) + "...";
    }
}