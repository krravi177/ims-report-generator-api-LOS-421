package com.xpanse.ims.report.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.xpanse.ims.report.constants.Constants;
import com.xpanse.ims.report.exception.DocumentException;
import com.xpanse.ims.report.response.DocumentResponse;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
public class DocumentService {

	private static final Logger logger = LogManager.getLogger(DocumentService.class);

	private final String getDocumentEndpoint;
	private final S3Service s3Service;
	private final DynamoDBService dynamoDBService;

	@Autowired
	public DocumentService(S3Service s3Service, DynamoDBService dynamoDBService,
			@Value("${getDocument.endpoint}") final String getDocumentEndpoint) {
		this.s3Service = s3Service;
		this.dynamoDBService = dynamoDBService;
		this.getDocumentEndpoint = getDocumentEndpoint;
	}

	public List<DocumentResponse> uploadDocuments(List<MultipartFile> files, String imsId, String tenantId) {
		
	    List<DocumentResponse> responses = new ArrayList<>();
	    
	    for (MultipartFile file : files) {
	    	
	        if (file.isEmpty()) {
	            logger.warn("Skipping empty file in upload for imsId: {}, tenantId: {}", imsId, tenantId);
	            responses.add(new DocumentResponse("Upload skipped: Empty file provided", "", ""));
	            continue;
	        }

	        String documentId = file.getOriginalFilename();
	        logger.info("Uploading document with ID: {} for tenant: {}", documentId, tenantId);

	        try {
	            // Upload to S3
	            logger.debug("Uploading document to S3 with key: {}", documentId);
	            String s3Key = s3Service.uploadDocument(file, documentId, imsId, tenantId);
	            logger.info("Document uploaded to S3 with key: {}", s3Key);

	            // Save document metadata in DynamoDB
	            logger.debug("Saving document metadata to DynamoDB for document ID: {}", documentId);
	            dynamoDBService.saveResourceMetadata(documentId, Constants.DOCUMENT, Constants.S3, s3Key, tenantId);
	            logger.info("Document metadata saved to DynamoDB for document ID: {}", documentId);

	            // Generate document URL
	            String documentUrl = String.format(getDocumentEndpoint, tenantId, documentId);
	            logger.info("Document successfully uploaded with URL: {}", documentUrl);

	            responses.add(new DocumentResponse("File uploaded successfully", documentUrl, documentId));
	        } catch (Exception e) {
	            logger.error("Error uploading document with ID: {} for tenant: {} - {}", documentId, tenantId, e.getMessage(), e);
	            responses.add(new DocumentResponse("Upload failed: " + e.getMessage(), "", documentId));
	        }
	    }

	    return responses;
	}


	public ResponseEntity<Object> getDocument(String documentId, String tenantId) {
		logger.info("Fetching document with ID: {} for tenant: {}", documentId, tenantId);

		try {
			// Retrieve the document metadata from DynamoDB
			logger.debug("Retrieving metadata from DynamoDB for document ID: {}", documentId);
			Map<String, AttributeValue> item = dynamoDBService.getResourceMetadata(documentId, tenantId);

			if (item == null || !item.containsKey(Constants.ID)) {
				String errorMessage = "Document not found for ID: " + documentId;
				logger.warn(errorMessage);
				throw new DocumentException(errorMessage);
			}

			String s3Key = item.get(Constants.STORAGE_ADDRESS).s();
			logger.info("Metadata retrieved. Fetching document from S3 with key: {}", s3Key);

			// Retrieve the document from S3
			ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Service.getDocument(s3Key, tenantId);
			InputStreamResource inputStreamResource = new InputStreamResource(s3ObjectStream);

			// Set headers for download
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + s3Key.substring(s3Key.lastIndexOf("/") + 1));
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

			logger.info("Document successfully retrieved for ID: {}", documentId);
			return ResponseEntity.ok()
					.headers(headers)
					.body(inputStreamResource);
		} catch (Exception e) {
			logger.error("Error fetching document with ID: {} for tenant: {} - {}", documentId, tenantId, e.getMessage(), e);
			throw new DocumentException("Error fetching document: " + e.getMessage(), e);
		}
	}
}
