package com.xpanse.ims.report.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.xpanse.ims.report.exception.DocumentException;
import com.xpanse.ims.report.exception.ErrorResponse;
import com.xpanse.ims.report.response.DocumentResponse;
import com.xpanse.ims.report.service.DocumentService;
import com.xpanse.ims.report.service.ReportGeneratorService;

@RestController
public class ReportGeneratorController {

	private static final Logger logger = LogManager.getLogger(ReportGeneratorController.class);

	private final ReportGeneratorService reportGeneratorService;
	private final DocumentService documentService;

	public ReportGeneratorController(ReportGeneratorService reportGeneratorService, DocumentService documentService) {
		this.reportGeneratorService = reportGeneratorService;
		this.documentService = documentService;
	}

	@PostMapping("/reports")
	public ResponseEntity<List<DocumentResponse>> generateReport(@RequestBody Map<String, Object> requestData, 
			@RequestHeader("X-Tenant-ID") String tenantId) throws IOException {

		logger.info("Received request to generate report for requestData: {}", requestData);

		try {

			String imsId = (String) requestData.get("imsId");
			List<DocumentResponse> documentResponses = reportGeneratorService.generateAndUploadPdf(requestData, imsId, tenantId);

			return ResponseEntity.ok(documentResponses);

		} catch (DocumentException e) {
			logger.error("Failed to upload documents: {}", e.getMessage(), e);
			List<DocumentResponse> errorResponse = List.of(
					new DocumentResponse("Upload failed: " + e.getMessage(), "", ""));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
	
	@GetMapping("/tenant/{tenantId}/documents/{documentId}")
	public ResponseEntity<Object> getDocument(@PathVariable String tenantId, 
			@PathVariable String documentId) {
		logger.info("Received request to retrieve document with ID: {} for tenantId: {}", documentId, tenantId);

		try {
			return documentService.getDocument(documentId, tenantId);
		} catch (DocumentException e) {
			logger.error("Failed to retrieve document: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Failed to retrieve document", e.getMessage()));
		} catch (Exception e) {
			logger.error("Unexpected error occurred: {}", e.getMessage(), e);
			// Handle unexpected exceptions
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Internal Server Error", "An unexpected error occurred."));
		}
	}
}
