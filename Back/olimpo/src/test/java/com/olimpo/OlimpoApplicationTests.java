package com.olimpo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.olimpo.service.EmailService;

@SpringBootTest(properties = {
    "api.security.token.secret=test-secret-key",
    "CLOUDINARY_CLOUD_NAME=test-cloud",
    "CLOUDINARY_API_KEY=123456789",
    "CLOUDINARY_API_SECRET=test-secret"
})
class OlimpoApplicationTests {

    @MockBean
    private EmailService emailService;

	@Test
	void contextLoads() {
	}

}