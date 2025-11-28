package com.olimpo.service;

import com.cloudinary.Cloudinary;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.repository.IdeaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final IdeaFileRepository ideaFileRepository;

    public IdeaFile uploadFile(MultipartFile file, Idea idea) throws IOException {
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser nulo ou vazio");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), Map.of(
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

        private String extractPublicIdFromUrl(String url) {
        Pattern pattern = Pattern.compile(".*/upload/(?:v\\d+/)?(.+)\\.[a-z]+$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}