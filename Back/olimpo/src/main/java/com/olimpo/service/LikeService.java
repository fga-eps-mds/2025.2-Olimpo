package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Like;
import com.olimpo.models.LikeId;
import com.olimpo.repository.UserRepository;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private IdeaRepository ideaRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public boolean toggleLike(Integer ideaId, Integer accountId) {
        LikeId id = new LikeId(accountId, ideaId);
        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
            return false; // Unliked
        } else {
            Account account = userRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            Idea idea = ideaRepository.findById(ideaId)
                    .orElseThrow(() -> new RuntimeException("Idea not found"));

            Like like = new Like(id, account, idea);
            likeRepository.save(like);
            return true; // Liked
        }
    }

    public long getLikeCount(Integer ideaId) {
        return likeRepository.countByIdeaId(ideaId);
    }

    public boolean isLikedByAccount(Integer ideaId, Integer accountId) {
        return likeRepository.existsById(new LikeId(accountId, ideaId));
    }
}
