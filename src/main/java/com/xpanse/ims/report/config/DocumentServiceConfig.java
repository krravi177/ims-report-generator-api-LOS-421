package com.xpanse.ims.report.config;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class DocumentServiceConfig {

	private final Region region;

	public DocumentServiceConfig(@Value("${sqs.region}") String region) {
		this.region = Region.of(region);
	}

	// Bean configuration for S3 client
	@Bean
	public S3Client s3Client() {
		return S3Client.builder().credentialsProvider(DefaultCredentialsProvider.create()).region(region).build();
	}

	// Bean configuration for DynamoDB client
	@Bean
	public DynamoDbClient dynamoDbClient() {
		return DynamoDbClient.builder().credentialsProvider(DefaultCredentialsProvider.create()).region(region).build();
	}
	
	@Bean
    public VelocityEngine velocityEngine() {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init(properties);

        return velocityEngine;
    }

}
