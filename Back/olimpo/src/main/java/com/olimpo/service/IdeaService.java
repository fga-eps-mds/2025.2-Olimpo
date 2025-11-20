package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Keyword;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.IdeaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public IdeaService(IdeaRepository ideaRepository, 
                       UserRepository accountRepository, 
                       KeywordRepository keywordRepository,
                       CloudinaryService cloudinaryService) {
        this.ideaRepository = ideaRepository;
        this.accountRepository = accountRepository;
        this.keywordRepository = keywordRepository;
        this.cloudinaryService = cloudinaryService;
    }

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

        return ideaRepository.save(idea);
    }

    public List<Idea> getAllIdeas() {
        return ideaRepository.findAll();
    }

    public Idea getIdeaById(Integer id) {
        return ideaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Idea não encontrada com id: " + id));
    }

    public Idea updateIdea(Integer id, Idea ideaDetails) {
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

        return ideaRepository.save(existingIdea);
    }

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