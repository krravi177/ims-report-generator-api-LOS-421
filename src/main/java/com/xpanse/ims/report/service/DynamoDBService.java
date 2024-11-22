package com.xpanse.ims.report.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.xpanse.ims.report.constants.Constants;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Service
public class DynamoDBService {

	private final DynamoDbClient dynamoDbClient;
	private final String resourceTableName;

	@Autowired
	public DynamoDBService(DynamoDbClient dynamoDbClient, 
			@Value("${aws.dynamodb.fileResource.table}") final String resourceTableName) {
		this.dynamoDbClient = dynamoDbClient;
		this.resourceTableName = resourceTableName;
	}

	public void saveResourceMetadata(String documentId, String resourceType, String storageType, String storageAddress, String tenantId) {
		Map<String, AttributeValue> itemValues = new HashMap<>();
		itemValues.put(Constants.ID, AttributeValue.builder().s(documentId).build());
		itemValues.put(Constants.RESOURCE_TYPE, AttributeValue.builder().s(resourceType).build());
		itemValues.put(Constants.TENANT_ID, AttributeValue.builder().s(tenantId).build());
		itemValues.put(Constants.STORAGE_ADDRESS, AttributeValue.builder().s(storageAddress).build());
		itemValues.put(Constants.STORAGE_TYPE, AttributeValue.builder().s(storageType).build());

		PutItemRequest request = PutItemRequest.builder()
				.tableName(resourceTableName)
				.item(itemValues)
				.build();
		dynamoDbClient.putItem(request);
	}

	public Map<String, AttributeValue> getResourceMetadata(String documentId, String tenantId) {
		Map<String, AttributeValue> key = new HashMap<>();
		key.put(Constants.ID, AttributeValue.builder().s(documentId).build());
		key.put(Constants.TENANT_ID, AttributeValue.builder().s(tenantId).build());

		GetItemRequest request = GetItemRequest.builder()
				.tableName(resourceTableName)
				.key(key)
				.build();

		return dynamoDbClient.getItem(request).item();
	}
}
