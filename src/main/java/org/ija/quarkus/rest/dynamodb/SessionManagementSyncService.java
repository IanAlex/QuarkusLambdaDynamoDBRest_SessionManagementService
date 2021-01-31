package org.ija.quarkus.rest.dynamodb;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import javax.inject.Inject;

@ApplicationScoped
public class SessionManagementSyncService extends AbstractService {
	
	@Inject
    DynamoDbClient dynamoDB;
    
    public void createTableIfNotExist() {
    	// Create the DynamDB table with getTable() if it is not there
    	// If the table already exists then the method immediately returns
    	String tableName = getTableName();
    	List<String> tableList = dynamoDB.listTables().tableNames();
    	if (tableList.contains(tableName)) {
    		System.out.println("Table " + tableName + " exists");
    		return;  //table exists
    	}
    	System.out.println("Creating table " + tableName);
    	//table doesnt exist so create it.
    	
    	
    	CreateTableRequest request = CreateTableRequest.builder()
                
                
                .attributeDefinitions(
                		AttributeDefinition.builder()
                        .attributeName(SESSION_ID_COL)
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                        AttributeDefinition.builder()
                        .attributeName(SERVICE_NAME_COL)
                        .attributeType(ScalarAttributeType.S)
                        .build())                   
                .keySchema(KeySchemaElement.builder()
                        .attributeName(SESSION_ID_COL)
                        .keyType(KeyType.HASH)
                        .build(),
                        KeySchemaElement.builder()
                        .attributeName(SERVICE_NAME_COL)
                        .keyType(KeyType.RANGE)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder().
                		readCapacityUnits(5L).
                		writeCapacityUnits(5L)
                		.build())
                .tableName(tableName)
                .build();  
    	
    	dynamoDB.createTable(request);
    }

    public List<SessionManagementRecord> findAll() {
    	createTableIfNotExist();
        return dynamoDB.scan(scanRequest()).items().stream()
                .map(SessionManagementRecord::from)
                .collect(Collectors.toList());
    }
    
    //If invoking service is not "A", "B" or "C" then return empty list
    //If invoking service is "A" or "B" then only return list of sessionIds corresponding to serviceNames "A" and "B" 
    //If invoking service is "C" then only return list of sessionIds corresponding to serviceName "C"
    public List<SessionManagementRecord> fetchAll(String callingServiceName) {
    	if (callingServiceName == null || (!callingServiceName.equals("A") && !callingServiceName.equals("B") && !callingServiceName.equals("C"))) {
    		List<SessionManagementRecord> retRecs = new ArrayList<>();
    		return retRecs;
    	}
    	List<SessionManagementRecord> retRecs = findAll();
    	if (retRecs == null || retRecs.isEmpty()) {
    		return retRecs;
    	}
    	
    	Iterator<SessionManagementRecord> iterator = retRecs.iterator();
    	while (iterator.hasNext()) {
    		SessionManagementRecord smr = iterator.next();
    		if ((callingServiceName.equals("A") || callingServiceName.equals("B")) && 
    				smr.getServiceName().equals("C")) 
    		{
    			iterator.remove();
    		}
    		else if (callingServiceName.equals("C") && 
    				(smr.getServiceName().equals("A") || smr.getServiceName().equals("B")) ) 
    		{
    			iterator.remove();
    		}
    	}
    	return retRecs;
    }
    
    //Fetch session management data for serviceName A only
    //If callingServiceName is not "A" or "B" then return empty list
    public List<SessionManagementRecord> fetchForServiceA(String callingServiceName) {
    	if (callingServiceName == null || (!callingServiceName.equals("A") && !callingServiceName.equals("B"))) {
    		List<SessionManagementRecord> retRecs = new ArrayList<>();
    		return retRecs;
    	}
    	List<SessionManagementRecord> retRecs = findAll();
    	if (retRecs == null || retRecs.isEmpty()) {
    		return retRecs;
    	}
    	
    	Iterator<SessionManagementRecord> iterator = retRecs.iterator();
    	while (iterator.hasNext()) {
    		SessionManagementRecord smr = iterator.next();
    		if (!smr.getServiceName().equals("A")) {
    			iterator.remove();
    		}
    	}
    	return retRecs;
    	
    }
    
    //Fetch session management data for serviceName B only
    //If callingServiceName is not "A" or "B" then return empty list
    public List<SessionManagementRecord> fetchForServiceB(String callingServiceName) {
    	if (callingServiceName == null || (!callingServiceName.equals("A") && !callingServiceName.equals("B"))) {
    		List<SessionManagementRecord> retRecs = new ArrayList<>();
    		return retRecs;
    	}
    	List<SessionManagementRecord> retRecs = findAll();
    	if (retRecs == null || retRecs.isEmpty()) {
    		return retRecs;
    	}
    	
    	Iterator<SessionManagementRecord> iterator = retRecs.iterator();
    	while (iterator.hasNext()) {
    		SessionManagementRecord smr = iterator.next();
    		if (!smr.getServiceName().equals("B")) {
    			iterator.remove();
    		}
    	}
    	return retRecs;
    	
    }
    
    //Fetch session management data for serviceName C only
    //If callingServiceName is not "C" then return empty list
    public List<SessionManagementRecord> fetchForServiceC(String callingServiceName) {
    	if (callingServiceName == null || !callingServiceName.equals("C")) {
    		List<SessionManagementRecord> retRecs = new ArrayList<>();
    		return retRecs;
    	}
    	List<SessionManagementRecord> retRecs = findAll();
    	if (retRecs == null || retRecs.isEmpty()) {
    		return retRecs;
    	}
    	
    	Iterator<SessionManagementRecord> iterator = retRecs.iterator();
    	while (iterator.hasNext()) {
    		SessionManagementRecord smr = iterator.next();
    		if (!smr.getServiceName().equals("C")) {
    			iterator.remove();
    		}
    	}
    	return retRecs;
    	
    }

    public SessionManagementRecord add(SessionManagementRecord smr) {
    	createTableIfNotExist();
        dynamoDB.putItem(putRequest(smr));
       
        return fetch(smr.getSessionId(), smr.getServiceName());
    }

    public SessionManagementRecord get(String sessionId, String serviceName) {
    	createTableIfNotExist();
        return SessionManagementRecord.from(dynamoDB.getItem(getRequest(sessionId, serviceName)).item());
    }
    
    //If callingServiceName is not "A", "B" or "C" then return null record    
    //otherwise return the record corresponding callingServiceName it exists (null otherwise)
    public SessionManagementRecord fetch(String sessionId, String callingServiceName) {
    	if (callingServiceName == null || (!callingServiceName.equals("A") && !callingServiceName.equals("B") && !callingServiceName.equals("C"))) {
    		return null;
    	}
    	return get(sessionId, callingServiceName);
    }

}
