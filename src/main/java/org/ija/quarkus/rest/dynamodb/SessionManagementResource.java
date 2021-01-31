package org.ija.quarkus.rest.dynamodb;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Path("/sessions")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class SessionManagementResource {
	
    @Inject
    SessionManagementSyncService service;
    
    @Context
    private HttpServletRequest request;

    @GET
    @Path("all/{callingServiceName}")
    // corresponds to: curl -v http://localhost:3000/sessions/all/{callingServiceName}
    public List<SessionManagementRecord> fetchAll(@PathParam("callingServiceName") String callingServiceName) {
    	System.out.println("Returning all allowed Session Management records for callingServiceName = " + callingServiceName);
    	if (callingServiceName == null || callingServiceName.trim().length() == 0) {
    		return null; 
    	}
    	return service.fetchAll(callingServiceName);
    }
    
    @GET
    @Path("A/{callingServiceName}")
  //corresponds to: curl -v http://localhost:3000/sessions/A/{callingServiceName}
    public List<SessionManagementRecord> fetchForServiceA(@PathParam("callingServiceName") String callingServiceName) {
    	System.out.println("Returning service A Session Management records for callingServiceName = " + callingServiceName + " if access allowed");
    	return service.fetchForServiceA(callingServiceName);
    }
    
    @GET
    @Path("B/{callingServiceName}")
  //corresponds to: curl -v http://localhost:3000/sessions/B/{callingServiceName}
    public List<SessionManagementRecord> fetchForServiceB(@PathParam("callingServiceName") String callingServiceName) {
    	System.out.println("Returning service B Session Management records for callingServiceName = " + callingServiceName + " if access allowed");
    	return service.fetchForServiceB(callingServiceName);
    }
    
    @GET
    @Path("C/{callingServiceName}")
    //corresponds to: curl -v http://localhost:3000/sessions/C/{callingServiceName}
    public List<SessionManagementRecord> fetchForServiceC(@PathParam("callingServiceName") String callingServiceName) {
    	System.out.println("Returning service C Session Management records for callingServiceName = " + callingServiceName + " if access allowed");
    	return service.fetchForServiceC(callingServiceName);
    }
    
    @GET
    @Path("{sessionId}/{callingServiceName}")
  //corresponds to: curl -v http://localhost:3000/sessions/{sessionId}/{callingServiceName}
    public SessionManagementRecord fetchForSessionId(@PathParam("sessionId") String sessionId, @PathParam("callingServiceName") String callingServiceName) {
    	System.out.println("Returning  Session Management records for sessionId = " + sessionId + " callingServiceName = " + callingServiceName + " if sessionId exists for this callingService");
    	return service.fetch(sessionId, callingServiceName);
    }
    
    
    @POST
    @Path("{callingServiceName}")
    //corresponds to: curl -X POST  http://localhost:3000/sessions/{callingServiceName}
    public SessionManagementRecord add(@PathParam("callingServiceName") String callingServiceName) {
    	SessionManagementRecord smr = new SessionManagementRecord();
    	HttpSession session = request.getSession();
    	SessionManagementRecord createdSMR = null;
    	if (callingServiceName != null && callingServiceName.trim().length() > 0 && session != null) {
    	   System.out.println("Attempting to write Session Management Record for callingServiceName = " + callingServiceName + 
    			    " and sessionId = " + session.getId());
    	
    	   smr.setSessionId(session.getId());
           smr.setServiceName(callingServiceName);
        
           createdSMR = service.add(smr);
    	}
    	if (createdSMR != null) {
    		System.out.println("A Session Management Record was created");
    	}
    	else {
    		System.out.println("No Session Management Record created");
    	}
    	return createdSMR;
    }
}