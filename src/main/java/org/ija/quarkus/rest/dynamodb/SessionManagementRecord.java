package org.ija.quarkus.rest.dynamodb;

import java.util.Map;
import java.util.Objects;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@RegisterForReflection
//SessionManagementRecord in DynamoDB has key of sessionId and non-key
//attribute of serviceName that accessed the DB.
public class SessionManagementRecord {
	
	private String sessionId;
	private String serviceName;
	
	public SessionManagementRecord() {
		
	}
	
    public static SessionManagementRecord from(Map<String, AttributeValue> item) {
    	SessionManagementRecord smr = new SessionManagementRecord();
        if (item != null && !item.isEmpty()) {
            smr.setSessionId(item.get(AbstractService.SESSION_ID_COL).s());
            smr.setServiceName(item.get(AbstractService.SERVICE_NAME_COL).s());
        }
        return smr;
    }

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	
	
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SessionManagementRecord)) {
            return false;
        }

        SessionManagementRecord other = (SessionManagementRecord) obj;

        return Objects.equals(other.sessionId, this.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sessionId);
    }

}
