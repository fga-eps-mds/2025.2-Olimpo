package com.olimpo.controller;

import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.repository.IdeaFileRepository;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaFileController {

    private final CloudinaryService cloudinaryService;
    private final IdeaRepository ideaRepository;
    private final IdeaFileRepository ideaFileRepository;

    @PostMapping("/{ideaId}/upload")
    public ResponseEntity<?> uploadIdeaFiles(
            @PathVariable Integer ideaId,
            @RequestParam("files") List<MultipartFile> files) {

        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new RuntimeException("Idea não encontrada com id: " + ideaId));

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nenhum arquivo enviado."));
        }

        List<String> fileUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    IdeaFile ideaFile = cloudinaryService.uploadFile(file, idea);
                    fileUrls.add(ideaFile.getFileUrl());
                }
            }
            return ResponseEntity.ok(Map.of("message", "Arquivos enviados com sucesso!", "fileUrls", fileUrls));
        
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Falha no upload do arquivo: " + e.getMessage()));
        }
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Integer fileId) {
        try {
            IdeaFile file = ideaFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Arquivo não encontrado"));

            cloudinaryService.deleteFile(file.getFileUrl());

            ideaFileRepository.delete(file);

            return ResponseEntity.ok(Map.of("message", "Arquivo deletado com sucesso"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao deletar arquivo: " + e.getMessage()));
        }
    }
}