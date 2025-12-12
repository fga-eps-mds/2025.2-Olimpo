package com.olimpo.dto;

public record ProfileUpdateDTO(
        String name,
        String email,
        String estado,
        String faculdade,
        Integer semestre,
        String curso,
        String bio,
        String docType,
        String docNumber
) {
}
