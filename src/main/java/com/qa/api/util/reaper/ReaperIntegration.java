package com.qa.api.util.reaper;

import io.rcrm.api.pojo.reaper.CreateEmailTemplateRequestBody;
import io.rcrm.api.pojo.reaper.NylasEmailConnect;
import io.rcrm.api.pojo.reaper.NylasEmailDisconnect;
import io.rcrm.api.pojo.reaper.UpdateEntityRequest;
import io.rcrm.api.restclient.RestClient;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReaperIntegration {

    private ReaperIntegration() {
        // private constructor to hide the implicit public one
    }

    static final String REAPER_BASE_URL = "https://reaper.recruitcrm.net/";
    static String dbname = System.getProperty("dbname");
    static String reaperUsername = System.getProperty("reaper_username");
    static String reaperPassword = System.getProperty("reaper_password");
    static Map<String, String> authTokenMap = new HashMap<String, String>();

    static {
        authTokenMap.put("reaper_username", reaperUsername);
        authTokenMap.put("reaper_password", reaperPassword);
    }

    
    static final String VMS_AUTH_BASE_URL = (
            System.getProperty("vms_auth_base_url", "https://vms-test2-auth.recruitcrm.io/"));

    

    public static boolean isReaperUp() {
        //Reaper Health Check Call
        Response checkCallResponse = RestAssured.get(REAPER_BASE_URL);
        if (checkCallResponse.statusCode() == 200) {
            String checkCallResponseBody = checkCallResponse.getBody().asString();
            return true;
        } else {
            return false;
        }
    }

    //Use getAccounts() method mentioned in the BaseTest class to get an account instead of this method
    public static Response getAccount(String accountType, int numberOfAccounts) {
        return getAccount(accountType, numberOfAccounts, "");
    }

    public static Response getAccount(String accountType, int numberOfAccounts, String flagName) {
        String endpointName = "createAccounts";
        String endUrl = endpointName + "/" + dbname + "/" + accountType + "/" + numberOfAccounts;

        JSONObject requestBody = null;
        if (flagName != null && !flagName.isEmpty()) {
            requestBody = new JSONObject();
            requestBody.put("flagName", flagName);
        }

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, requestBody);

        if (response.statusCode() != 200) {
            String errorMessage = "Failed to create account with payload. Status code: " + response.statusCode() + ", Response: " + response.getBody().prettyPrint();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    public static Response getRbacAccount(String accountType, String flagName, int numberOfAccounts, boolean rbac) {
        //Replace endpointName with 'createAccountsFromStoredProcedure' to create account with store procedure using reaper
        String endpointName = "createAccounts";

        //Reaper Create Account Call
        String endUrl = endpointName + "/" + dbname + "/" + accountType + "/" + numberOfAccounts;

        // Create request body with rbac parameter using JSONObject
        JSONObject requestBody = new JSONObject();
        requestBody.put("rbac", rbac);
        if (!flagName.isEmpty()) {
            requestBody.put("flagName", flagName);
        }

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, requestBody);

        if (response.statusCode() != 200) {
            String errorMessage = "Failed to create account with payload. Status code: " + response.statusCode() + ", Response: " + response.getBody().prettyPrint();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    public static void deactivateMultipleAccounts(String[] accountIdArray) {
        StringBuilder commaSeperatedAccountIds = new StringBuilder();
        try {
            if (accountIdArray == null || accountIdArray.length == 0) {
                return;
            }
            for (int i = 0; i < accountIdArray.length; i++) {
                if (accountIdArray[i].equals("0")) {
                }
                if (i == accountIdArray.length - 1) {
                    commaSeperatedAccountIds.append(accountIdArray[i]);
                } else {
                    commaSeperatedAccountIds.append(accountIdArray[i]).append(",");
                }
            }
        } catch (NumberFormatException e) {
        }

        // Use 'deleteAccountFromStoredProcedure' in endpointName to delete account with store procedure using reaper
        String endpointName = "deleteMultipleAccounts";

        String endUrl = endpointName + "/" + dbname + "/" + commaSeperatedAccountIds;

        Response response = RestClient.doDelete("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, null, false);

        if (response.statusCode() != 200) {
        }
    }

    public static void deactivateAccount(int accountId) {
        if (accountId == 0) {
            return;
        }

        //Use this url to deactivate one account using reaper
        String endUrl = "deleteAccount/" + dbname + "/" + accountId;

        Response response = RestClient.doDelete("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, null, false);

        if (response.statusCode() != 200) {
        }
    }

    public static Response connectEmail(int accountId, String email, String password, String emailType,
                                        int linkedEmailType, int isDefault, int roleId){
        int maxRetries = 2;
        long initialBackoffMillis = 1000; // Initial backoff duration in milliseconds
        Response lastResponse = null;

        NylasEmailConnect nylasEmailConnect = new NylasEmailConnect(emailType, email, password, linkedEmailType, isDefault, roleId);
        String endUrl =  "connectEmail/" + dbname + "/" + accountId;

        for (int retryCount = 0; retryCount <= maxRetries; retryCount++) {
            lastResponse = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, nylasEmailConnect);
            if(lastResponse.statusCode() < 500){
                break;
            }
            long backoffMillis = initialBackoffMillis * (long) Math.pow(2, retryCount);
            System.out.println("Retry #" + (retryCount + 1) + ", Backoff: " + backoffMillis + "ms");
            try {
                Thread.sleep(backoffMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if(lastResponse.statusCode() == 200){
            System.out.println(lastResponse.getBody().asString());
            if(linkedEmailType == 1){
                if (roleId == 4) {
                    ThreadManager.getOwner().setConnectedEmail_1(email);
                } else if (roleId == 2) {
                    ThreadManager.getAdmin().setConnectedEmail_1(email);
                } else if (roleId == 3) {
                    ThreadManager.getTeamMember().setConnectedEmail_1(email);
                } else if (roleId == 5) {
                    ThreadManager.getRestrictedTeamMember().setConnectedEmail_1(email);
                }
            }else if(linkedEmailType == 2){
                if (roleId == 4) {
                    ThreadManager.getOwner().setConnectedEmail_2(email);
                } else if (roleId == 2) {
                    ThreadManager.getAdmin().setConnectedEmail_2(email);
                } else if (roleId == 3) {
                    ThreadManager.getTeamMember().setConnectedEmail_2(email);
                } else if (roleId == 5) {
                    ThreadManager.getRestrictedTeamMember().setConnectedEmail_2(email);
                }
            }
        }else{
            System.err.println("Error: " + lastResponse.statusCode() + " " + lastResponse.getBody().prettyPrint());
        }
        return lastResponse;
    }

    public static Response updateSeqEnrollmentSteps(int seqEnrollmentId) {
        String endUrl = "updateSeqEnrollmentSteps/" + dbname + "/" + seqEnrollmentId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response.statusCode() == 200) {
        } else {
        }
        return response;
    }
    
    public static Response nylasEmailDisconnect(int accountId, int linkedEmailType, int notify) {
        if (accountId == 0) {
            return null;
        }

        String endUrl = "disconnectEmail/" + dbname + "/" + accountId;

        NylasEmailDisconnect nylasEmailDisconnect = new NylasEmailDisconnect(linkedEmailType, notify);

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, nylasEmailDisconnect);

        if (response.statusCode() != 200) {
        }else {
            if(linkedEmailType == 1){
                ThreadManager.getOwner().setConnectedEmail_1(null);
            }else if(linkedEmailType == 2){
                ThreadManager.getOwner().setConnectedEmail_2(null);
            }
        }
        return response;
    }

    public static void pauseEnrollment(int enrollmentId) {
        if (enrollmentId == 0) {
            return;
        }

        String endUrl = "pauseEnrollment/" + dbname + "/" + enrollmentId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);

        if (response.statusCode() != 200) {
        }
    }

    public static void failScheduledEmail(int emailId) {
        if (emailId == 0) {
            return;
        }
        
        String endUrl = "scheduledEmail/fail/" + dbname + "/" + emailId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);

        if (response.statusCode() != 200) {
        }
    }
    
    public static void logAccountEntry(int accountId) {
    	 if (accountId == 0) {
             return;
         }
    	 
        String endUrl = "logAccountEntry/" + dbname + "/" + accountId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        
        if (response.statusCode() == 200) {
        } else {
        }
    }

    public static Response updateTwilioSubaccount(int accountId) {
        if (accountId == 0) {
            return null;
        }

        String endUrl = "updateSmsSubAccount/" + dbname + "/" + accountId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response insertPurchasedNumber(int accountId,int userId) {
        if (accountId == 0) {
            return null;
        }

        String endUrl = "insertPurchasedPhoneNumber/" + dbname + "/" + accountId + "/" + userId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response enableA2PRegistration(int accountId) {
        if (accountId == 0) {
            return null;
        }

        String endUrl = "enableA2PRegistration/" + dbname + "/" + accountId;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response optOutFromSms(String entityType, String slug) {
        if (entityType == null || entityType.isEmpty() || slug == null || slug.isEmpty()) {
            return null;
        }

        String endUrl = "optOutFromSms/" + dbname + "/" + entityType + "/" + slug;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response updateTwilioCredits(int accountId, int credits) {
        String endUrl = "/updateTwilioCredits" + "/" + dbname + "/" + accountId + "/" + credits;
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response provideSmsConsentToEntity(int accountId, String entityType, String slug) {
        String endUrl = "/provideSmsConsentToEntity" + "/" + dbname + "/" + accountId + "/" + entityType + "/" + slug;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response.statusCode() != 200) {
        }
        return response;
    }

    public static Response updateEntityColumns(String slug, UpdateEntityRequest updateEntityRequest) {
        String endUrl = "updateEntityColumns/" + dbname + "/" + slug;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, updateEntityRequest);
        Assert.assertEquals(response.getStatusCode(), 200, "Response code must be 200");
        return response;
    }
    public static Response insertUnipileSubscription(int accountId, String email, int userId) {
        if (accountId == 0 || dbname == null || dbname.isEmpty() || email == null || email.trim().isEmpty() || userId == 0) {
            System.out.println("Error! Please provide valid env, accountId, email, and userId.");
            return null;
        }
        String endUrl = String.format("insertUnipileSubscription/%s/%d/%s/%d", dbname, accountId, email, userId);
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response == null) {
            throw new AssertionError("insertUnipileSubscription: Response is null");
        }
        if (response.statusCode() != 200) {
            throw new AssertionError("insertUnipileSubscription: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response updateUnipileSubscriptionStatus(int accountId) {
        if (accountId == 0 || dbname == null || dbname.isEmpty()) {
            System.out.println("Error! Please provide valid env and accountId.");
            return null;
        }
        String endUrl = String.format("updateUnipileSubscriptionStatus/%s/%d", dbname, accountId);
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response == null) {
            throw new AssertionError("updateUnipileSubscriptionStatus: Response is null");
        }
        if (response.statusCode() != 200) {
            throw new AssertionError("updateUnipileSubscriptionStatus: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response createDummyUnipileUserInfo(int accountId, int userId) {

        if (accountId == 0 || dbname == null || dbname.isEmpty() || userId == 0) {
            System.out.println("Error! Please provide valid env and accountId.");
            return null;
        }
        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("accountId", accountId);

        String endUrl = "createDummyUnipileUserInfo/" + dbname;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);
        if (response.statusCode() != 200) {
            throw new AssertionError("createDummyUnipileUserInfo: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response enableAuditLog(int accountId) {
		String endUrl = "/enableAuditLog" + "/" + dbname + "/" + accountId;

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);

		if (response.statusCode() != 200) {
			System.err.println("Error: " + response.statusCode());
		}
		return response;
	}

    public static Response getCurrentUserDetailsId(int userId) {

        String endUrl = "getUserDetailsID/" + dbname + "/" + userId;

        Response response = RestClient.doGet("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, null, true);
        if (response.statusCode() != 200) {
            throw new AssertionError("Error getting current user details ID. Expected 200 but got: " + response.statusCode());
        }
        return response;
    }

    public static Response insertBulkRecords(int accountId, String entity, int count) {
        
        String endUrl = "/insertBulkRecords" + "/" + dbname + "/" + accountId + "/" + entity + "/" + count;

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, null);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Expected HTTP 200 OK, but got " + response.statusCode());
        }
        return response;
    }


    public static Response updateLastActivityTimestamp(String entityName, int entityId, JSONObject fieldsAndTimestamps) {
        JSONObject payload = new JSONObject();
        payload.put("entityName", entityName);
        payload.put("entityId", entityId);
        payload.put("fieldsAndTimestamps", fieldsAndTimestamps);
        String endURL = "/updateLastActivityTimestamps/" + dbname;
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endURL, authTokenMap, null, true, payload);
        if (response.statusCode() != 200) {
            throw new AssertionError("updateLastActivityTimestamp: Expected status 200 but got " + response.statusCode() +
                    " for entity: " + entityName + ", id: " + entityId + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response updateCandidateTimestamp(int id, JSONObject fields) {
        JSONObject payload = new JSONObject();
        payload.put("candidateId", id);
        payload.put("fieldsAndTimestamps", fields);
        String endURL = "/updateCandidateTimestamps/" + dbname;
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endURL, authTokenMap, null, true, payload);
        if (response.statusCode() != 200) {
            throw new AssertionError("updateCandidateTimestamp: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response updateActivityTimestamp(int id, JSONObject timestamp, String activityName) {
        JSONObject payload = new JSONObject();
        payload.put("activityName", activityName);
        payload.put("id", id);
        payload.put("createdOn", timestamp.getString("createdOn"));
        payload.put("updatedOn", timestamp.getString("updatedOn"));
        if (timestamp.has("startdate")) {
            payload.put("startdate", timestamp.getString("startdate"));
        }
        String endURL = "/updateActivityTimestamps/" + dbname;
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endURL, authTokenMap, null, true, payload);
        if (response.statusCode() != 200) {
            throw new AssertionError("updateActivityTimestamp: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
        return response;
    }

    public static Response updateHotlistTimestamp(int hotlistId, String created_on, String updated_on) {
        String endUrl = "/updateHotlist" + "/" + dbname + "/" + hotlistId;
		JSONObject payload = new JSONObject();
		payload.put("created_on", Integer.parseInt(created_on));
		payload.put("updated_on", Integer.parseInt(updated_on));

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);

		if (response.statusCode() != 200) {
			String errorMessage = "Failed to update hotlist. Status code: " + response.statusCode() +
					", Response: " + response.getBody().prettyPrint();
			throw new RuntimeException(errorMessage);
		}
		return response;
    }

    public static Response updateSavedSearchTimestamp(int savedSearchId, String created_on, String updated_on) {
        String endUrl = "/updateSavedSearches" + "/" + dbname + "/" + savedSearchId;
		JSONObject payload = new JSONObject();
		payload.put("created_on", Integer.parseInt(created_on));
		payload.put("updated_on", Integer.parseInt(updated_on));

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);

		if (response.statusCode() != 200) {
			String errorMessage = "Failed to update saved search. Status code: " + response.statusCode() +
					", Response: " + response.getBody().prettyPrint();
			throw new RuntimeException(errorMessage);
		}
		return response;
    }

	public static Response createEmailTemplate(CreateEmailTemplateRequestBody createEmailTemplateRequestBody) {
		String endpointName = "createEmailTemplate";
		String endUrl = endpointName + "/" + dbname;

		// Create request body with email template data
		JSONObject requestBody = new JSONObject();
		requestBody.put("emailcontext", createEmailTemplateRequestBody.getEmailcontext());
		requestBody.put("emailsubject", createEmailTemplateRequestBody.getEmailsubject());
		requestBody.put("template", createEmailTemplateRequestBody.getTemplate());
		requestBody.put("accountid", createEmailTemplateRequestBody.getAccountid());
		requestBody.put("createdby", createEmailTemplateRequestBody.getCreatedby());
		requestBody.put("relatedtotypeid", createEmailTemplateRequestBody.getRelatedtotypeid());
		requestBody.put("share", createEmailTemplateRequestBody.getShare());

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, requestBody);

		if (response.statusCode() != 200) {
			System.out.println("Failed to create email template. Status: " + response.statusCode() +
				", Response: " + response.getBody().asString());
		}

		return response;
	}

	public static Response getEntityIdFromSlug(String tableName, String slug) {
		if (tableName == null || tableName.trim().isEmpty() || slug == null || slug.trim().isEmpty()) {
			return null;
		}

		String endUrl = "getEntityIdFromSlug/" + dbname;

		// Create request body
		JSONObject requestBody = new JSONObject();
		requestBody.put("tableName", tableName);
		requestBody.put("slug", slug);

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, requestBody);

		if (response.statusCode() != 200) {
			throw new AssertionError("Error getting entity ID from slug. Status: " + response.statusCode() + ", Response: " + response.getBody().asString());
		}
		return response;
	}

    
    public static Response createClientPortalAccount(int entityId, String firstName, String lastName,
            String email, int rcrmAccountId, int jobId, boolean vmsRcrmJobLink, String companyName, int rcrmCompanyId, String rcrmEmailID, int rcrmUserId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", 1);
        requestBody.put("entityId", entityId);
        requestBody.put("firstName", firstName);
        requestBody.put("lastName", lastName);
        requestBody.put("email", email);
        requestBody.put("rcrmAccountId", rcrmAccountId);
        requestBody.put("jobID", jobId);
        requestBody.put("vms_rcrm_job_link", vmsRcrmJobLink);
        requestBody.put("companyName", companyName);
        requestBody.put("rcrmCompanyId", rcrmCompanyId);
        requestBody.put("rcrmEmailID", rcrmEmailID);
        requestBody.put("rcrmUserId", rcrmUserId);

        String endUrl = "createAccount/client-portal";
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL , endUrl,
                authTokenMap, null, true, requestBody);

        if (response.getStatusCode() != 200) {
            String errorMessage = "Failed to create portal account. Status code: " + response.getStatusCode()
                    + ", Response: " + response.getBody().asString();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    
    public static Response createContractorPortalAccount(int entityId, String firstName, String lastName, String email, int rcrmAccountId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("entityType", 3);
        requestBody.put("entityId", entityId);
        requestBody.put("firstName", firstName);
        requestBody.put("lastName", lastName);
        requestBody.put("email", email);
        requestBody.put("rcrmAccountId", rcrmAccountId);

        String endUrl = "createAccount/contractor-portal";
        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl,
                authTokenMap, null, true, requestBody);

        if (response.getStatusCode() != 200) {
            String errorMessage = "Failed to create contractor portal account. Status code: "
                    + response.getStatusCode() + ", Response: " + response.getBody().asString();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

   

    private static Response vmsPortalLogin(String path, String username) {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("password", "123456");
        body.put("rememberMe", false);
        body.put("client_id", "vms-webapp");

        return RestClient.doPost("JSON", VMS_AUTH_BASE_URL, path, Collections.<String, String>emptyMap(), null,
                true, body);
    }

    
    public static Response createClientPortalAccountAndStoreContext(int entityId, String firstName,
            String lastName, String email, int rcrmAccountId, int jobId, boolean vmsRcrmJobLink, String companyName,
            int rcrmCompanyId, String rcrmEmailID, int rcrmUserId) {
        Response response = createClientPortalAccount(entityId, firstName, lastName, email, rcrmAccountId, jobId,
                true, companyName, rcrmCompanyId, rcrmEmailID, rcrmUserId);
        PortalThreadManager.applyCreateAccountResponse(response, entityId, rcrmCompanyId);
        return response;
    }

    public static Response createContractorPortalAccountAndStoreContext(int entityId, String firstName,
        String lastName, String email, int rcrmAccountId) {
        Response response = createContractorPortalAccount(entityId, firstName, lastName, email, rcrmAccountId);
        PortalThreadManager.applyCreateContractorAccountResponse(response);
        return response;
    }

    
    public static Response vmsContractorPortalLoginAndStore(String username) {
        Response response = vmsPortalLogin("v1/contractor/login", username);
        PortalThreadManager.applyLoginResponse(response);
        return response;
    }

    public static Response vmsClientPortalLoginAndStore(String username) {
        Response response = vmsPortalLogin("v1/login", username);
        PortalThreadManager.applyLoginResponse(response);
        return response;
    }

    public static Response updateCompanyFields(int companyId, JSONObject fieldsAndValues) {
        String endUrl = "updateCompany/" + dbname + "/" + companyId;
        JSONObject payload = new JSONObject();
        payload.put("fieldsAndValues", fieldsAndValues);

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);

        if (response.statusCode() != 200) {
            String errorMessage = "Failed to update company fields. Status code: " + response.statusCode() +
                    ", Response: " + response.getBody().prettyPrint();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    public static Response updateContactFields(int contactId, JSONObject fieldsAndValues) {
        String endUrl = "updateContact/" + dbname + "/" + contactId;
        JSONObject payload = new JSONObject();
        payload.put("fieldsAndValues", fieldsAndValues);

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);  

        if (response.statusCode() != 200) {
            String errorMessage = "Failed to update contact fields. Status code: " + response.statusCode() +
                    ", Response: " + response.getBody().prettyPrint();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    public static Response updateJobFields(int jobId, JSONObject fieldsAndValues) {
        String endUrl = "updateJob/" + dbname + "/" + jobId;
        JSONObject payload = new JSONObject();
        payload.put("fieldsAndValues", fieldsAndValues);

        Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, true, payload);

        if (response.statusCode() != 200) {
            String errorMessage = "Failed to update job fields. Status code: " + response.statusCode() +
                    ", Response: " + response.getBody().prettyPrint();
            throw new RuntimeException(errorMessage);
        }
        return response;
    }

    public static Response updateAutomatedCallLog(int accountId) {
		String endUrl = "updateAutomatedCallLog/" + dbname + "/" + accountId;

		Response response = RestClient.doPost("JSON", REAPER_BASE_URL, endUrl, authTokenMap, null, false, null);
		if (response.statusCode() != 200) {
            throw new AssertionError("updateAutomatedCallLog: Expected status 200 but got " + response.statusCode() + ". Response: " + response.getBody().asString());
        }
		return response;
	}
}
