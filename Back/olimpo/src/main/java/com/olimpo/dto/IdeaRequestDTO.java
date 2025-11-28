package com.olimpo.dto;

import java.util.List;

public record IdeaRequestDTO(
    String name,
    String description,
    Integer price,
    List<String> keywords
) {}