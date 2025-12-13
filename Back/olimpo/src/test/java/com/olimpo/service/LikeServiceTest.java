package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.Like;
import com.olimpo.models.LikeId;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.LikeRepository;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private UserRepository accountRepository;

    @Mock
    private com.olimpo.repository.NotificationRepository notificationRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void toggleLike_ShouldLike_WhenNotLiked() {
        Integer ideaId = 1;
        Integer accountId = 1;
        LikeId likeId = new LikeId(accountId, ideaId);

        Account liker = new Account();
        liker.setId(accountId);

        Account owner = new Account();
        owner.setId(2);

        Idea idea = new Idea();
        idea.setAccount(owner);

        when(likeRepository.existsById(likeId)).thenReturn(false);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(liker));
        when(ideaRepository.findById(ideaId)).thenReturn(Optional.of(idea));

        boolean result = likeService.toggleLike(ideaId, accountId);

        assertTrue(result);
        verify(likeRepository).save(any(Like.class));
        verify(notificationRepository).save(any(com.olimpo.models.Notification.class));
    }

    @Test
    void toggleLike_ShouldUnlike_WhenAlreadyLiked() {
        Integer ideaId = 1;
        Integer accountId = 1;
        LikeId likeId = new LikeId(accountId, ideaId);

        when(likeRepository.existsById(likeId)).thenReturn(true);

        boolean result = likeService.toggleLike(ideaId, accountId);

        assertFalse(result);
        verify(likeRepository).deleteById(likeId);
    }

    @Test
    void getLikeCount_ShouldReturnCount() {
        Integer ideaId = 1;
        when(likeRepository.countByIdeaId(ideaId)).thenReturn(5L);

        long count = likeService.getLikeCount(ideaId);

        assertEquals(5L, count);
    }

    @Test
    void isLikedByAccount_ShouldReturnTrue_WhenExists() {
        Integer ideaId = 1;
        Integer accountId = 1;
        LikeId likeId = new LikeId(accountId, ideaId);

        when(likeRepository.existsById(likeId)).thenReturn(true);

        boolean result = likeService.isLikedByAccount(ideaId, accountId);

        assertTrue(result);
    }
}
