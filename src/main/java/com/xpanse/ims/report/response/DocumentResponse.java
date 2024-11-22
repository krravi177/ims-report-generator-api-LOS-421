package com.xpanse.ims.report.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

	private String message;
	private String documentUrl;
	private String documentId;


}