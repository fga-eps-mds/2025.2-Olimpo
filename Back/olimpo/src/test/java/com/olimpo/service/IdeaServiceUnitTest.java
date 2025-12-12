package com.olimpo.service;

import com.olimpo.models.Account;
import com.olimpo.models.Idea;
import com.olimpo.models.IdeaFile;
import com.olimpo.models.Keyword;
import com.olimpo.models.Like;
import com.olimpo.repository.IdeaRepository;
import com.olimpo.repository.KeywordRepository;
import com.olimpo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IdeaServiceUnitTest {

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private com.olimpo.repository.LikeRepository likeRepository;

    @InjectMocks
    private IdeaService ideaService;

    @Test
    void createIdea_AssociatesKeywordsAndSaves() {
        Account owner = new Account();
        owner.setId(5);

        Idea idea = new Idea();
        idea.setName("Name");
        idea.setDescription("Desc");
        idea.setPrice(10);

        Keyword k1 = new Keyword("k1");
        k1.setId(1);
        Keyword k2 = new Keyword("k2");
        k2.setId(2);

        idea.setKeywords(new HashSet<>(Arrays.asList(k1, k2)));

        when(userRepository.findById(5)).thenReturn(Optional.of(owner));
        when(keywordRepository.findAllById(anySet())).thenReturn(Arrays.asList(k1, k2));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(inv -> inv.getArgument(0));

        Idea saved = ideaService.createIdea(idea, 5);

        assertNotNull(saved.getAccount());
        assertEquals(owner, saved.getAccount());
        assertEquals(2, saved.getKeywords().size());
        verify(ideaRepository).save(any(Idea.class));
    }

    @Test
    void updateIdea_WithNewFile_ReplacesOldAndSaves() throws IOException {
        Idea existing = new Idea();
        existing.setId(10);
        IdeaFile oldFile = new IdeaFile(existing, "old.png", "image/png", "https://res.cloudinary.com/test/upload/v123/old.png");
        existing.getIdeaFiles().add(oldFile);

        Idea details = new Idea();
        details.setName("Updated");
        details.setDescription("Upd");
        details.setPrice(50);

        MultipartFile newFile = mock(MultipartFile.class);
        when(newFile.isEmpty()).thenReturn(false);

        IdeaFile uploaded = new IdeaFile(existing, "new.png", "image/png", "https://res.cloudinary.com/test/upload/v123/new.png");

        when(ideaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(cloudinaryService.uploadFile(newFile, existing)).thenReturn(uploaded);
        when(ideaRepository.save(any(Idea.class))).thenAnswer(inv -> inv.getArgument(0));

        Idea result = ideaService.updateIdea(10, details, newFile);

        // old file should have been deleted and new one added
        verify(cloudinaryService).deleteFile(oldFile.getFileUrl());
        verify(cloudinaryService).uploadFile(newFile, existing);
        verify(ideaRepository).save(existing);
        assertEquals("Updated", result.getName());
        assertEquals(1, result.getIdeaFiles().size());
    }

    @Test
    void deleteIdea_DeletesFilesAndEntity() throws IOException {
        Idea idea = new Idea();
        idea.setId(20);
        IdeaFile f1 = new IdeaFile(idea, "a.png", "image/png", "https://res.cloudinary.com/test/upload/v1/a.png");
        IdeaFile f2 = new IdeaFile(idea, "b.png", "image/png", "https://res.cloudinary.com/test/upload/v1/b.png");
        idea.getIdeaFiles().addAll(Arrays.asList(f1, f2));

        when(ideaRepository.findById(20)).thenReturn(Optional.of(idea));

        ideaService.deleteIdea(20);

        verify(cloudinaryService).deleteFile(f1.getFileUrl());
        verify(cloudinaryService).deleteFile(f2.getFileUrl());
        verify(ideaRepository).delete(idea);
    }

    @Test
    void getLikedIdeas_ReturnsLikedIdeas() {
        Integer accountId = 1;
        Account account = new Account();
        account.setId(accountId);

        Idea idea1 = new Idea();
        idea1.setId(1);
        idea1.setName("Idea 1");

        Idea idea2 = new Idea();
        idea2.setId(2);
        idea2.setName("Idea 2");

        Like like1 = new Like();
        like1.setIdea(idea1);
        Like like2 = new Like();
        like2.setIdea(idea2);

        List<Like> likes = Arrays.asList(like1, like2);

        when(likeRepository.findByAccountId(accountId)).thenReturn(likes);

        List<Idea> result = ideaService.getLikedIdeas(accountId);

        assertEquals(2, result.size());
        assertEquals(idea1, result.get(0));
        assertEquals(idea2, result.get(1));
        verify(likeRepository).findByAccountId(accountId);
    }

    @Test
    void getAllIdeas_ReturnsAllIdeas() {
        Idea idea1 = new Idea();
        idea1.setId(1);
        Idea idea2 = new Idea();
        idea2.setId(2);

        List<Idea> ideas = Arrays.asList(idea1, idea2);

        when(ideaRepository.findAllWithDetails()).thenReturn(ideas);

        List<Idea> result = ideaService.getAllIdeas();

        assertEquals(2, result.size());
        verify(ideaRepository).findAllWithDetails();
    }

    @Test
    void getIdeaById_ReturnsIdea_WhenExists() {
        Integer id = 1;
        Idea idea = new Idea();
        idea.setId(id);

        when(ideaRepository.findById(id)).thenReturn(Optional.of(idea));

        Idea result = ideaService.getIdeaById(id);

        assertEquals(idea, result);
        verify(ideaRepository).findById(id);
    }

    @Test
    void getIdeaById_ThrowsException_WhenNotExists() {
        Integer id = 1;

        when(ideaRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ideaService.getIdeaById(id);
        });

        assertEquals("Idea não encontrada com id: " + id, exception.getMessage());
        verify(ideaRepository).findById(id);
    }

    @Test
    void createIdea_ThrowsException_WhenAccountNotFound() {
        Integer accountId = 5;
        Idea idea = new Idea();

        when(userRepository.findById(accountId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ideaService.createIdea(idea, accountId);
        });

        assertEquals("Account não encontrado com id: " + accountId, exception.getMessage());
        verify(userRepository).findById(accountId);
        verify(ideaRepository, never()).save(any());
    }

    @Test
    void updateIdea_WithoutNewFile_UpdatesDetails() throws IOException {
        Idea existing = new Idea();
        existing.setId(10);
        existing.setName("Old Name");
        existing.setDescription("Old Desc");
        existing.setPrice(10);

        Idea details = new Idea();
        details.setName("New Name");
        details.setDescription("New Desc");
        details.setPrice(20);

        when(ideaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(inv -> inv.getArgument(0));

        Idea result = ideaService.updateIdea(10, details, null);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals(20, result.getPrice());
        verify(ideaRepository).save(existing);
        verify(cloudinaryService, never()).uploadFile(any(), any());
    }

    @Test
    void updateIdea_WithKeywords_UpdatesKeywords() throws IOException {
        Idea existing = new Idea();
        existing.setId(10);

        Idea details = new Idea();
        details.setName("Updated");

        Keyword k1 = new Keyword("k1");
        k1.setId(1);
        Keyword k2 = new Keyword("k2");
        k2.setId(2);

        details.setKeywords(new HashSet<>(Arrays.asList(k1, k2)));

        when(ideaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(keywordRepository.findAllById(anySet())).thenReturn(Arrays.asList(k1, k2));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(inv -> inv.getArgument(0));

        Idea result = ideaService.updateIdea(10, details, null);

        assertEquals(2, result.getKeywords().size());
        verify(keywordRepository).findAllById(anySet());
    }

    @Test
    void updateIdea_WithEmptyKeywords_ClearsKeywords() throws IOException {
        Idea existing = new Idea();
        existing.setId(10);
        existing.setKeywords(new HashSet<>(Arrays.asList(new Keyword("old"))));

        Idea details = new Idea();
        details.setName("Updated");
        details.setKeywords(new HashSet<>()); // empty

        when(ideaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(inv -> inv.getArgument(0));

        Idea result = ideaService.updateIdea(10, details, null);

        assertTrue(result.getKeywords().isEmpty());
    }
}
