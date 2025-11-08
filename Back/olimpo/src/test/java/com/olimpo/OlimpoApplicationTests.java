package com.olimpo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.olimpo.service.EmailService;

@SpringBootTest
class OlimpoApplicationTests {

    @MockBean
    private EmailService emailService;

	@Test
	void contextLoads() {
	}

}