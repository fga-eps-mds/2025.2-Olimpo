package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.IdeaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final UserRepository accountRepository;
    private final KeywordRepository keywordRepository;
    private final CloudinaryService cloudinaryService;

    private final com.olimpo.repository.LikeRepository likeRepository;

    @Autowired
    public IdeaService(IdeaRepository ideaRepository,
            UserRepository accountRepository,
            KeywordRepository keywordRepository,
            CloudinaryService cloudinaryService,
            com.olimpo.repository.LikeRepository likeRepository) {
        this.ideaRepository = ideaRepository;
        this.accountRepository = accountRepository;
        this.keywordRepository = keywordRepository;
        this.cloudinaryService = cloudinaryService;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public List<Idea> getLikedIdeas(Integer accountId) {
        List<com.olimpo.models.Like> likes = likeRepository.findByAccountId(accountId);
        List<Idea> ideas = likes.stream().map(com.olimpo.models.Like::getIdea).collect(Collectors.toList());

        for (Idea idea : ideas) {
            if (idea.getKeywords() != null)
                idea.getKeywords().size();
            if (idea.getIdeaFiles() != null)
                idea.getIdeaFiles().size();
        }

        return ideas;
    }

    @Transactional
    public Idea createIdea(Idea idea, Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account não encontrado com id: " + accountId));
        idea.setAccount(account);

        if (idea.getKeywords() != null && !idea.getKeywords().isEmpty()) {
            Set<Integer> keywordIds = idea.getKeywords().stream()
                    .map(Keyword::getId)
                    .collect(Collectors.toSet());
            Set<Keyword> managedKeywords = new HashSet<>(keywordRepository.findAllById(keywordIds));
            idea.setKeywords(managedKeywords);
        }

        Idea savedIdea = ideaRepository.save(idea);

        if (savedIdea.getKeywords() != null)
            savedIdea.getKeywords().size();
        if (savedIdea.getIdeaFiles() != null)
            savedIdea.getIdeaFiles().size();

        return savedIdea;
    }

    public List<Idea> getAllIdeas() {
        return ideaRepository.findAllWithDetails();
    }

    public Idea getIdeaById(Integer id) {
        return ideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Idea não encontrada com id: " + id));
    }

    @Transactional
    public Idea updateIdea(Integer id, Idea ideaDetails, MultipartFile newFile) throws IOException {
        Idea existingIdea = getIdeaById(id);

        existingIdea.setName(ideaDetails.getName());
        existingIdea.setDescription(ideaDetails.getDescription());
        existingIdea.setPrice(ideaDetails.getPrice());

        if (ideaDetails.getKeywords() != null) {
            Set<Integer> keywordIds = ideaDetails.getKeywords().stream()
                    .map(Keyword::getId)
                    .collect(Collectors.toSet());
            Set<Keyword> managedKeywords = new HashSet<>(keywordRepository.findAllById(keywordIds));
            existingIdea.setKeywords(managedKeywords);
        } else {
            existingIdea.setKeywords(new HashSet<>());
        }

        if (existingIdea.getIdeaFiles() != null) {
            existingIdea.getIdeaFiles().size();
        }

        if (newFile != null && !newFile.isEmpty()) {
            if (existingIdea.getIdeaFiles() != null && !existingIdea.getIdeaFiles().isEmpty()) {
                for (var oldFile : existingIdea.getIdeaFiles()) {
                    cloudinaryService.deleteFile(oldFile.getFileUrl());
                }
                existingIdea.getIdeaFiles().clear();
            }

            IdeaFile newIdeaFile = cloudinaryService.uploadFile(newFile, existingIdea);
            existingIdea.getIdeaFiles().add(newIdeaFile);
        }

        return ideaRepository.save(existingIdea);
    }

    @Transactional
    public void deleteIdea(Integer id) {
        Idea idea = getIdeaById(id);

        if (idea.getIdeaFiles() != null) {
            for (var file : idea.getIdeaFiles()) {
                try {
                    cloudinaryService.deleteFile(file.getFileUrl());
                } catch (IOException e) {
                    System.err.println("Erro ao deletar arquivo do Cloudinary: " + file.getFileUrl());
                }
            }
        }
        ideaRepository.delete(idea);
    }
}