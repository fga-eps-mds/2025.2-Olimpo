package com.olimpo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.olimpo.repository.IdeaFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private IdeaFileRepository ideaFileRepository;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void deleteFile_DeveExtrairIdPublicoCorretamente_EChamarDestroy() throws IOException {
        when(cloudinary.uploader()).thenReturn(uploader);
        String fileUrl = "http://res.cloudinary.com/demo/image/upload/v1312461204/olimpo/ideas/1/sample.jpg";
        String expectedPublicId = "olimpo/ideas/1/sample";

        cloudinaryService.deleteFile(fileUrl);

        verify(uploader).destroy(eq(expectedPublicId), anyMap());

    }

    @Test
    void deleteFile_NaoDeveChamarDestroy_SeUrlForInvalida() throws IOException {
        
        String invalidUrl = "http://google.com/imagem-aleatoria.jpg";

        cloudinaryService.deleteFile(invalidUrl);

        verify(uploader, never()).destroy(anyString(), anyMap());
        
    }
}
