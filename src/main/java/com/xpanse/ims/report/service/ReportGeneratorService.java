package com.xpanse.ims.report.service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.xpanse.ims.report.response.DocumentResponse;


@Service
public class ReportGeneratorService {

	private final VelocityEngine velocityEngine;
	private final DocumentService documentService;

	public ReportGeneratorService(VelocityEngine velocityEngine, DocumentService documentService) {
		this.velocityEngine = velocityEngine;
		this.documentService = documentService;
	}
	
	public List<DocumentResponse> generateAndUploadPdf(Map<String, Object> data, String imsId, String tenantId) {
	    try {
	        MultipartFile pdfFile = createPdfAsMultipartFile(data);
	        List<MultipartFile> files = List.of(pdfFile);  // Single-file list
	        return documentService.uploadDocuments(files, imsId, tenantId); // Upload using the existing method
	    } catch (IOException e) {
	        return List.of(new DocumentResponse("PDF generation failed", "", ""));
	    }
	}
	
	public MultipartFile createPdfAsMultipartFile(Map<String, Object> data) throws IOException {
	    ByteArrayOutputStream pdfOutputStream = createPdfFromTemplate(data);

	    // Convert ByteArrayOutputStream to MultipartFile
	    return new MockMultipartFile("generatedReport", "InstantIncomeReport.pdf", "application/pdf", pdfOutputStream.toByteArray());
	}

	public ByteArrayOutputStream createPdfFromTemplate(Map<String, Object> data) throws IOException {
		// Prepare Velocity context with data
		VelocityContext context = new VelocityContext(data);

		// Load and render the template
		StringWriter writer = new StringWriter();
		velocityEngine.getTemplate("templates/InstantIncomeReportTemplate.vm").merge(context, writer);
		String htmlContent = writer.toString();
		htmlContent = htmlContent.replace("\r", "");
		
		// Create a PDF from HTML content using Flying Saucer
		ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
		try (FileOutputStream outputStream = new FileOutputStream("InstantIncomeReport.pdf")) {
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(htmlContent);
			renderer.layout();
			renderer.createPDF(pdfOutputStream);

			pdfOutputStream.writeTo(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Failed to generate PDF from HTML", e);
		}

		return pdfOutputStream;

	}

}
