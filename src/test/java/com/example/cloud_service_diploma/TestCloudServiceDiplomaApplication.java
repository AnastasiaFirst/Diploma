package com.example.cloud_service_diploma;

import org.springframework.boot.SpringApplication;

public class TestCloudServiceDiplomaApplication {

	public static void main(String[] args) {
		SpringApplication.from(CloudServiceDiplomaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
