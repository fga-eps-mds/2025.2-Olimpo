package com.olimpo.service;

import com.cloudinary.Cloudinary;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.repository.IdeaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final long MAX_PROFILE_IMAGE_BYTES = 5 * 1024 * 1024; // 5MB

    private final Cloudinary cloudinary;
    private final IdeaFileRepository ideaFileRepository;

    public IdeaFile uploadFile(MultipartFile file, Idea idea) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser nulo ou vazio");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of(
            "resource_type", "auto",
            "folder", "olimpo/ideas/" + idea.getId()
        ));

        String fileUrl = (String) uploadResult.get("secure_url");
        String originalName = file.getOriginalFilename();
        String fileType = file.getContentType();
        
        IdeaFile ideaFile = new IdeaFile(idea, originalName, fileType, fileUrl);
        return ideaFileRepository.save(ideaFile);
    }

    public void deleteFile(String fileUrl) throws IOException {
        String publicId = extractPublicIdFromUrl(fileUrl);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, Map.of());
        }
    }

    public String uploadProfilePicture(MultipartFile file, Integer accountId) throws IOException {
        validateImageFile(file);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                Map.ofEntries(
                        Map.entry("resource_type", "image"),
                        Map.entry("folder", "olimpo/users/" + accountId),
                        Map.entry("public_id", "profile"),
                        Map.entry("overwrite", true)
                )
        );

        return (String) uploadResult.get("secure_url");
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem inválido");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos");
        }

        if (file.getSize() > MAX_PROFILE_IMAGE_BYTES) {
            throw new IllegalArgumentException("Imagem excede o limite de 5MB");
        }
    }

    private String extractPublicIdFromUrl(String url) {
        Pattern pattern = Pattern.compile(".*/upload/(?:v\\d+/)?(.+)\\.[a-z]+$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}