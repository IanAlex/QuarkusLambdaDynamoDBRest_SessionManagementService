package org.ija.quarkus.rest.dynamodb;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

public class AbstractService {
	
	public final static String SESSION_ID_COL = "sessionId";
    public final static String SERVICE_NAME_COL = "serviceName";
    
    public String getTableName() {
        return "IJAQuarkusSessionManagementDataD";
    }
    
    
    
    protected ScanRequest scanRequest() {
        return ScanRequest.builder().tableName(getTableName())
                .attributesToGet(SESSION_ID_COL, SERVICE_NAME_COL).build();
    }

    protected PutItemRequest putRequest(SessionManagementRecord smr) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(SESSION_ID_COL, AttributeValue.builder().s(smr.getSessionId()).build());
        item.put(SERVICE_NAME_COL, AttributeValue.builder().s(smr.getServiceName()).build());
          
        return PutItemRequest.builder()
                .tableName(getTableName())
                .item(item)
                .build();
    }

    protected GetItemRequest getRequest(String sessionId, String serviceName) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(SESSION_ID_COL, AttributeValue.builder().s(sessionId).build());
        key.put(SERVICE_NAME_COL, AttributeValue.builder().s(serviceName).build());

        return GetItemRequest.builder()
                .tableName(getTableName())
                .key(key)
                .attributesToGet(SESSION_ID_COL, SERVICE_NAME_COL)
                .build();
    }

}
