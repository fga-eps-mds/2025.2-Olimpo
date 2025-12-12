package com.olimpo.dto;

public record UserProfileDTO(
                Integer id,
                String name,
                String pfp,
                String bio,
                String role,
                String faculdade,
                String curso,
                String estado,
                String email,
                String phone) {
}
