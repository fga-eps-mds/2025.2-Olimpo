package com.olimpo.dto;

import com.olimpo.models.Enums.Role;

public record RegisterDTO(
                String email,
                String password,
                String name,
                String docType,
                String docNumber,
                Role role,
                String faculdade,
                String curso,
                String phone) {
}