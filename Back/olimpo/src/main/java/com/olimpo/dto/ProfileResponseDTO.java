package com.olimpo.dto;

import com.olimpo.models.Account;

public record ProfileResponseDTO(
        Integer id,
        String name,
        String email,
        boolean emailVerified,
        String pfp,
        String role,
        String estado,
        String faculdade,
        String curso,
        String bio,
        String docType,
        String docNumber,
        String phone) {
    public ProfileResponseDTO(Account account) {
        this(
                account.getId(),
                account.getName(),
                account.getEmail(),
                account.isEmailVerified(),
                account.getPfp(),
                account.getRole(),
                account.getEstado(),
                account.getFaculdade(),
                account.getCurso(),
                account.getBio(),
                account.getDocType(),
                account.getDocNumber(),
                account.getPhone());
    }
}
