package com.example.cloud_service_diploma;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CloudServiceDiplomaApplicationTests {

	@Test
	void contextLoads() {
	}

}
