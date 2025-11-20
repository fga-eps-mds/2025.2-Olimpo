package com.olimpo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olimpo.dto.IdeaRequestDTO;
import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.service.CloudinaryService;
import com.olimpo.service.IdeaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/ideas")
public class IdeaController {

    @Autowired
    private IdeaService ideaService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private KeywordRepository keywordRepository;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createIdea(
            @RequestPart("data") String ideaJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Account user 
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            IdeaRequestDTO dto = mapper.readValue(ideaJson, IdeaRequestDTO.class);

            Idea newIdea = new Idea();
            newIdea.setName(dto.name());
            newIdea.setDescription(dto.description());
            newIdea.setPrice(dto.price());

            if (dto.keywords() != null && !dto.keywords().isEmpty()) {
                Set<Keyword> keywords = new HashSet<>();
                for (String keywordName : dto.keywords()) {
                    keywordRepository.findByName(keywordName).ifPresent(keywords::add);
                }
                newIdea.setKeywords(keywords);
            }

            Idea savedIdea = ideaService.createIdea(newIdea, user.getId());

            if (file != null && !file.isEmpty()) {
                cloudinaryService.uploadFile(file, savedIdea);
            }

            return ResponseEntity.ok(savedIdea);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Erro ao processar JSON: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro interno: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllIdeas() {
        return ResponseEntity.ok(ideaService.getAllIdeas());
    }
}