package com.raisedeveloper.server.global.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
public class GcsConfig {

	@Value("${storage.gcs.project-id}")
	private String projectId;

	@Value("${storage.gcs.credentials-location}")
	private String credentialsLocation;

	@Bean
	public Storage gcsStorage() throws IOException {
		log.info("Initializing GCS Storage with project-id: {}", projectId);

		Credentials credentials = GoogleCredentials.fromStream(
			new FileInputStream(credentialsLocation)
		);

		return StorageOptions.newBuilder()
			.setProjectId(projectId)
			.setCredentials(credentials)
			.build()
			.getService();
	}
}
