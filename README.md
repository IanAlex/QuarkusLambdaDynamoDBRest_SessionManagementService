# ija-quarkus-rest-dynamodb project : Description and Assumptions

(i) This Session Management Service project uses AWS Quarkus Lambda with DynamoDB run locally with 
a docker container pointing to a local DynamoDB.   Please Note: the DynamoDB table for session management
records is programmatically created in the application if it does not already exist.  This
occurs on the first read (GET) or write (PUT) operation. 

(ii) the application is designed to simulate services named 'A', 'B' and 'C' 
each allowed to access the Session Management Service.  When services 'A', 'B' or 'C'
access the Session Management Service a record witht the sessionId (key) and serviceName
is written to the DynamoDB table.   For simplicity this is simulated by Curl POST commands
(see the Curl Notes below).  

(iii) when a Calling Service wants to access the Session Management Data in the Dynamo table 
then the following rules are followed:
               - service 'A' has access to only Session Management Records that were written
			   by services 'A' and 'B'
			   - service 'B' has access to only Session Management Records that were written
			   by services 'A' and 'B'
			   - service 'B' has access to only Session Management Records that were written
			   by service 'C'
			   
(iv) the retrieval of records by a calling service is simulated by Curl GETs a this retrieval
does NOT count as a write-access as well (i.e. if 'A' retrieves records from DynamoDB through
the Session Management Service this does NOT count as an additional access POST/write).
			   

#Steps to Build and Run the application

(assumes Maven and Docker for DynamoDB are installed)

(i) download the maven project (assumes Maven is installed on your machine)

(ii) edit the application.properties file to change the quarkus.dynamodb.endpoint-override
property to point to port 8000 on the machine it is being run from (currently it is 
http://192.168.0.10:8000/).  This DIFFERS from the https://quarkus.io/guides/amazon-dynamodb 
document that had it as http://localhost:8000 owing to errors generated - i.e. this is 
a WORKAROUND for running on a machine with docker-Dynamo running

(iii) in the ija-quarkus-rest-dynamodb project directory (containing the pom.xml file), 
run: mvn clean package

(iv) if there are no errors in the build then edit the target/sam.jvm.yaml file to change the
Timeout (under IjaQuarkusRestDynamodb:) from 15 to 100.   This is necessary because otherwise
most of the REST calls will time out after 15 seconds and not complete (100 seconds is more
than needed but provides a safety assurance)

(v) Run the application: in the ija-quarkus-rest-dynamodb director (perhaps in a separate command window) run
the command: sam local start-api --template target/sam.jvm.yaml 

(vi) test the application using Curl GET/POST commands as described in the next session

(vi) in command window for (v) hit CTL-C when want to stop testing

#Testing the Session Management Service
 
 This is done by a series of Curl POSTs and GETs as described below (with reference to 
 (iii) in the "Description and Assumptions" above
 
#POST commands:
 
 These are used for services to simulate "access" to the Session Management Service and write
 to the DynamoDB table.
 
 The Curl Post command is:
 
 curl -X POST  http://localhost:3000/sessions/{callingSessionName}
 
 where callingSessionName is the name of the calling service (e.g. 'A', 'B' or 'C').   This 
 command will result in a call to the SessionManagementResource.add() method
 
 The command will return the JSON representation of the record inserted.
 e.g. the command curl -X POST  http://localhost:3000/sessions/A
 will return something like:  {"sessionId":"VbhE6LvgcaTrAT8iCohxHNgV0nvGLiQDntX10NUq","serviceName":"A"}

 And in the console
 where the application is running there will be some System.out.println() messages spooled.
 
#GET commands:
 
 (I) Getting in JSON form  ALL Session Management Records accessible the calling Service.
 
       command in form:  curl -v http://localhost:3000/sessions/all/{callingService}
	   
	   This will result in a call to the SessionManagementResource.fetchAll() method in the Session Management Service app.
	   
	   e.g. curl -v http://localhost:3000/sessions/all/A	   
	   will return list of Session Management records (with sessionId & serviceName for each)
       for stored records of services 'A' and 'B'

       e.g. curl -v http://localhost:3000/sessions/all/C
       will return list of Session Management records (with sessionId & serviceName for each)
       for stored records of service 'C' only
	   
 (II) Getting in JSON form Session Management Records for a particular service:
 
       command in form: curl -v http://localhost:3000/sessions/A/{callingService}
	     to access all service 'A' session management records if accessible by callingService
		 This results in call to SessionManagementResource.fetchForServiceA()
		 
	   command in form: curl -v http://localhost:3000/sessions/B/{callingService}
	     to access all service 'B' session management records if accessible by callingService
		 This results in call to SessionManagementResource.fetchForServiceB()
		 
	   command in form: curl -v http://localhost:3000/sessions/C/{callingService}
	     to access all service 'C' session management records if accessible by callingService
		This results in call to SessionManagementResource.fetchForServiceC() 

	   e.g. curl -v http://localhost:3000/sessions/B/A and curl -v http://localhost:3000/sessions/B/B
	       will access all service 'B' session management records while curl -v http://localhost:3000/sessions/B/C
		   will return an empty list because service 'B' records are not accessible by callingService 'C'. However
		   curl -v http:/localhost:3000/sessions/B/C will return a list of all service 'C' records.
		   
 (III) Query to see if sessionId exists for a specific calling service:
    
	   command in form: curl -v http://localhost:3000/sessions/{sessionId}/{callingServiceName}
	   which results in a call to the SessionManagementResource.fetchForSessionId() in the Session
	   Management Service app.
	   
	   e.g. if there is a record in DynamoDB table for sessionID='XXXX' for calling service 'A' then
	        curl -v http://localhost:3000/sessions/XXXX/A
			
			will return that record. If the record doesn't exist then return null.
		   
#Test Scenario:
		   
curl -X POST  http://localhost:3000/sessions/A  (run twice)

curl -X POST  http://localhost:3000/sessions/B (run three times)

curl -X POST  http://localhost:3000/sessions/C (run once)

curl -v http://localhost:3000/sessions/all/A (returns 2 'A' and 3 'B' session management records)

curl -v http://localhost:3000/sessions/all/B (returns 2 'A' and 3 'B' session management records)

curl -v http://localhost:3000/sessions/all/C  (returns 1 'C' session management record)

curl -v http://localhost:3000/sessions/A/A  (returns 2 'A' session management records)

curl -v http://localhost:3000/sessions/A/B  (returns 2 'A' session management records)

curl -v http://localhost:3000/sessions/A/C  (returns empty list)

curl -v http://localhost:3000/sessions/B/A  (returns 3 'B' session management records)

curl -v http://localhost:3000/sessions/B/B  (returns 3 'B' session management records)

curl -v http://localhost:3000/sessions/B/C  (returns empty list)

curl -v http://localhost:3000/sessions/C/A  (returns empty list)

curl -v http://localhost:3000/sessions/C/B  (returns empty list)

curl -v http://localhost:3000/sessions/C/C  (returns 1 'C' session management record)

curl -v http://localhost:3000/sessions/XXXX/A 
(returns a session management record if there is a sessionId=XXXX record for serviceName=A
in DynamoDB table; otherwise returns null)
		   
		   
