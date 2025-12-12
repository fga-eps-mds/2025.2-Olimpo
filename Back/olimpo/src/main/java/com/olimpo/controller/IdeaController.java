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

    @Autowired
    private com.olimpo.service.LikeService likeService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> createIdea(
            @RequestPart("data") String ideaJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal Account user) {
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
    public ResponseEntity<?> getAllIdeas(@AuthenticationPrincipal Account user) {
        java.util.List<Idea> ideas = ideaService.getAllIdeas();
        java.util.List<IdeaResponseDTO> response = new java.util.ArrayList<>();

        for (Idea idea : ideas) {
            long likes = likeService.getLikeCount(idea.getId());
            boolean liked = false;
            if (user != null) {
                liked = likeService.isLikedByAccount(idea.getId(), user.getId());
            }
            response.add(new IdeaResponseDTO(idea, likes, liked));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/liked")
    public ResponseEntity<?> getLikedIdeas(@AuthenticationPrincipal Account user) {
        java.util.List<Idea> ideas = ideaService.getLikedIdeas(user.getId());
        java.util.List<IdeaResponseDTO> response = new java.util.ArrayList<>();

        for (Idea idea : ideas) {
            long likes = likeService.getLikeCount(idea.getId());
            response.add(new IdeaResponseDTO(idea, likes, true));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/liked")
    public ResponseEntity<?> getLikedIdeasForUser(@PathVariable Integer userId, @AuthenticationPrincipal Account user) {
        java.util.List<Idea> ideas = ideaService.getLikedIdeas(userId);
        java.util.List<IdeaResponseDTO> response = new java.util.ArrayList<>();

        for (Idea idea : ideas) {
            long likes = likeService.getLikeCount(idea.getId());
            boolean liked = false;
            if (user != null) {
                liked = likeService.isLikedByAccount(idea.getId(), user.getId());
            }
            response.add(new IdeaResponseDTO(idea, likes, liked));
        }
        return ResponseEntity.ok(response);
    }

    public record IdeaResponseDTO(Idea idea, long likeCount, boolean isLiked) {
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> updateIdea(
            @PathVariable Integer id,
            @RequestPart("data") String ideaJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            IdeaRequestDTO dto = mapper.readValue(ideaJson, IdeaRequestDTO.class);

            Idea ideaDetails = new Idea();
            ideaDetails.setName(dto.name());
            ideaDetails.setDescription(dto.description());
            ideaDetails.setPrice(dto.price());

            if (dto.keywords() != null) {
                Set<Keyword> keywords = new HashSet<>();
                for (String k : dto.keywords()) {
                    keywordRepository.findByName(k).ifPresent(keywords::add);
                }
                ideaDetails.setKeywords(keywords);
            }

            Idea updatedIdea = ideaService.updateIdea(id, ideaDetails, file);
            return ResponseEntity.ok(updatedIdea);

        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Erro ao processar JSON: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro interno: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIdea(@PathVariable Integer id) {
        try {
            ideaService.deleteIdea(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Integer id, @AuthenticationPrincipal Account user) {
        try {
            boolean liked = likeService.toggleLike(id, user.getId());
            return ResponseEntity.ok(liked);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<?> getLikes(@PathVariable Integer id, @AuthenticationPrincipal Account user) {
        try {
            long count = likeService.getLikeCount(id);
            boolean liked = false;
            if (user != null) {
                liked = likeService.isLikedByAccount(id, user.getId());
            }
            return ResponseEntity.ok(new LikeStatusDTO(count, liked));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public record LikeStatusDTO(long count, boolean liked) {
    }
}