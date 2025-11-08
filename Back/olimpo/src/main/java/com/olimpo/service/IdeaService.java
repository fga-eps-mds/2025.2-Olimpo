package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.IdeaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final UserRepository accountRepository;

    @Autowired
    public IdeaService(IdeaRepository ideaRepository, UserRepository accountRepository) {
        this.ideaRepository = ideaRepository;
        this.accountRepository = accountRepository;
    }

    public Idea createIdea(Idea idea, Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account não encontrado com id: " + accountId));
        idea.setAccount(account);
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

        return ideaRepository.save(existingIdea);
    }

    public void deleteIdea(Integer id) {
        if (!ideaRepository.existsById(id)) {
            throw new RuntimeException("Idea não encontrada com id: " + id);
        }
        ideaRepository.deleteById(id);
    }
}