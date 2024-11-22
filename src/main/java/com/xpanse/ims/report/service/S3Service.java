package com.xpanse.ims.report.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	private final S3Client s3Client;
	private final String bucket;

	@Autowired
	public S3Service(S3Client s3Client,
			@Value("${aws.s3.bucket.voie-documents}") final String bucket) {
		this.s3Client = s3Client;
		this.bucket = bucket;
	}

	public String uploadDocument(MultipartFile file, String documentId, String imsId, String tenantId) throws IOException {
		String key = tenantId +"/"+ imsId +"/"+ documentId;

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		return key;
	}

	public ResponseInputStream<GetObjectResponse> getDocument(String s3Key, String tenantId) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(tenantId)
				.key(s3Key)
				.build();

		return s3Client.getObject(getObjectRequest);
	}
}
