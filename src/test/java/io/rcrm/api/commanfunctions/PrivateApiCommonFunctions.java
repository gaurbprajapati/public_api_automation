package io.rcrm.api.commanfunctions;

import io.rcrm.api.pojo.albatross.CreateRoles;
import io.rcrm.api.pojo.albatross.RoleId;
import io.rcrm.api.pojo.albatross.User;
import io.rcrm.api.pojo.albatross.UserDetails;
import io.rcrm.api.pojo.albatross.UserRoleUpdate;
import io.rcrm.api.pojo.albatross.UsersGet;
import io.rcrm.api.pojo.albatross.deal.CreateDeal;
import io.rcrm.api.pojo.albatross.jobs.UpdateJobRequest;
import io.rcrm.api.pojo.albatross.jobs.JobUpdateData;
import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Private API Common Functions for user management and role operations
 * Provides methods for creating custom roles, managing user permissions, and role assignments
 */
public class PrivateApiCommonFunctions {
    private static final List<String> PERMISSION_KEYS = Arrays.asList(
            "recruiter_performance_report_access", "target_report_access", "reports", "target_report", 
            "target_report_create_access", "all_connected_email", "sent_email_kpi_report",
            "recruiter_performance_report", "candidate_lifecycle_report", "client_performance_report",
            "job_statistic_report", "time_to_hire_report", "deal_report", "account_overview_report",
            "deals_by_team", "export_to_csv", "bulk_update_field", "bulk_delete", "admin_settings",
            "plans_and_billing", "user_management", "ip_restriction", "roles_permissions", "teams",
            "account_management", "audit_log", "email_triggers", "sales_pipeline", "hiring_pipeline",
            "pitch_candidate_pipeline", "deals_pipeline", "job_status", "customize_invoice",
            "fields_shared_with_client", "call_type_customization", "note_type_customization",
            "meeting_type_customization", "task_type_customization", "candidate_fields", "company_fields",
            "contact_fields", "deal_fields", "job_fields", "public_job_page_settings", "job_application_form_settings",
            "profile_update_form_settings", "talent_pool_page_settings", "custom_xml_settings",
            "api_integrations", "job_board_integrations", "calling_integrations", "standard_email_templates",
            "resume_formatting", "hide_email", "blacklist_email_id", "sms_templates", "executive_search_report",
            "external_job_board_integration", "email_sequence", "activity_templates", "workflow_automation",
            "hotlist", "customize_off_limit_status", "record_calls_by_default", "allow_to_choose_call_record",
            "allow_to_download_records", "job_advertising", "advanced_analytics", "allow_to_choose_own_fields", "private_emails"
    );

    public Response createCustomRole(String albatross_url, String authTokenMap, String roleName, Map<String, String> entityAccess, Map<String, Object>  fieldsPermission) {
		int maxRetries = 3;
		int retryDelayMillis = 3000; // 1 second delay between retries

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				// Get the json structure of existing roles before manipulating them
				HashMap<String,String> token = new HashMap<>();
				token.put("Authorization", "Bearer " + authTokenMap);

				Response jsonStructureResponse = RestClient.doGet("JSON", albatross_url, "roles/default-access-control-json", token, null, null, false);

				// Convert jsonStructureResponse to JsonPath and fetch the records field, remove other albatross fields
				JsonPath jsonPath = jsonStructureResponse.jsonPath();

				Object userAccessJsonString = jsonStructureResponse.jsonPath().getMap("data");

				// Convert the userAccessJsonString to a Map<String, Object>
				Map<String, Object> userAccessJson = (Map<String, Object>) userAccessJsonString;

				// Call the updateFieldValue method with the userAccessJson
				updateFieldValue(userAccessJson, entityAccess, fieldsPermission);

				ObjectMapper objectMapper = new ObjectMapper();
				String modifiedUserAccessJson = null;
				try {
					modifiedUserAccessJson = objectMapper.writeValueAsString(userAccessJson);
				} catch (JsonProcessingException e) {
					throw new RuntimeException(e);
				}

				CreateRoles createRoles = new CreateRoles(null, roleName, modifiedUserAccessJson);

				// Send the POST
				Response response = RestClient.doPost("JSON", albatross_url, "roles", token, null, false, createRoles);

				return response;
			} catch (Exception e) {
				// Log the exception or handle it as needed

				if (attempt < maxRetries) {
					// If there are remaining attempts, wait before retrying
					try {
						Thread.sleep(retryDelayMillis);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				} else {
					// If no more attempts remaining, throw the exception
					throw e;
				}
			}
		}

		return null;
	}

    private static void updateFieldValue(Map<String, Object> requestBody, Map<String, String> entityAccess, Map<String, Object> straightforwardFields) {
		Set<String> validPermissionLevels = new HashSet<>(Arrays.asList(
				"Everything", "Team Only", "Owned Only", "Nothing", "Yes", "No"
		));
		for (Map.Entry<String, String> entry : entityAccess.entrySet()) {
			String fullKey = entry.getKey(); // e.g., "jobs" or "candidates.canedit"
			String newValue = entry.getValue();
			// Skip invalid values
			if (!validPermissionLevels.contains(newValue)) {
				continue;
			}
			if (fullKey.contains(".")) {
				String[] parts = fullKey.split("\\.");
				String parentKey = parts[0];
				String childKey = parts[1];
				if (requestBody.containsKey(parentKey)) {
					Map<String, String> childMap = (Map<String, String>) requestBody.get(parentKey);
					childMap.put(childKey, newValue);
					requestBody.put(parentKey, childMap);
				}
			} else {
				if (requestBody.containsKey(fullKey)) {
					Object obj = requestBody.get(fullKey);
					if (obj instanceof Map) {
						Map<String, String> childMap = (Map<String, String>) obj;
						Set<String> keys = new HashSet<>(childMap.keySet());
						for (String key : keys) {
							childMap.put(key, newValue);
						}
						requestBody.put(fullKey, childMap);
					}
				}
			}
		}
		// Update straightforward fields
		for (Map.Entry<String, Object> entry : straightforwardFields.entrySet()) {
			String fieldName = entry.getKey();
			Object newValue = entry.getValue();
			if (requestBody.containsKey(fieldName)) {
				requestBody.put(fieldName, newValue);
			}
		}
	}

    public int getRoleId(String albatross_url, String authTokenMap, String roleName) {
		int maxRetries = 3;
		int retryDelayMillis = 3000; // 1 second delay between retries

		for (int attempt = 1; attempt <= maxRetries; attempt++) {
			try {
				String jsonBody = "{\"sort_by\": \"updatedon\", \"sortOrder\": \"desc\", \"page\": 1, \"page_size\": \"14\", \"columns\": {}}";

				URL url = new URL(albatross_url + "/roles/get");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Authorization", "Bearer " + authTokenMap);

				connection.setDoOutput(true);

				// Writing request body
				try (OutputStream outputStream = connection.getOutputStream()) {
					byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
					outputStream.write(input, 0, input.length);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				// Reading response
				StringBuilder response = new StringBuilder();
				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String line;
					while ((line = in.readLine()) != null) {
						response.append(line);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					connection.disconnect();
				}

				JSONObject responseObject = new JSONObject(response.toString());

				JSONObject data = responseObject.getJSONObject("data");

				// Get the array of records
				JSONArray records = data.getJSONArray("records");

				// Iterate through each record to find the role ID
				for (int i = 0; i < records.length(); i++) {
					JSONObject record = records.getJSONObject(i);

					// Extract role information
					int roleId = record.getInt("id");
					String extractedRoleName = record.getString("role");
					// Check if the roleName matches the desired role
					if (roleName.equals(extractedRoleName)) {
						return roleId;
					}
				}

				// If role not found, break out of the loop
				break;
			} catch (Exception e) {
				// Log the exception or handle it as needed
				if (attempt < maxRetries) {
					// If there are remaining attempts, wait before retrying
					try {
						Thread.sleep(retryDelayMillis);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				} else {
					// If no more attempts remaining, throw the exception
					throw new RuntimeException(e);
				}
			}
		}

		// If role not found after all attempts, return 0
		return 0;
	}
	
	// Method to update user role by email (preferred approach)
	public Response updateRoleOfUserByEmail(String albatross_url, Object privateApiToken, int role_id, String new_roleName, String userEmail){
		//Get User Details
		UsersGet usersGet=new UsersGet();
		Map<String, String> authTokenMap = getAuthTokenMap(privateApiToken);
		usersGet.setPage_size("5");
		Response response=RestClient.doPost("JSON",albatross_url,"users/get",authTokenMap,null,false,usersGet);
		JsonPath usersDetails=response.jsonPath();
		
		// Extract records array and find user by email dynamically
		List<Map<String, Object>> records = usersDetails.get("data.records");
		Map<String, Object> targetUser = null;
		
		// Loop through records to find user with matching email
		for (Map<String, Object> user : records) {
			String userEmailFromRecord = (String) user.get("email");
			if (userEmail.equals(userEmailFromRecord)) {
				targetUser = user;
				break;
			}
		}
		
		// If no user found with the specified email, throw exception or handle gracefully
		if (targetUser == null) {
			throw new RuntimeException("No user found with email: " + userEmail);
		}
		
		// Extract user details from the matched user
		int user_id = (Integer) targetUser.get("id");
		String parameter = String.valueOf(targetUser.get("id"));
		String user_role = (String) targetUser.get("role");

		//Update User Role
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("user_id", parameter);

		//Creating User Role Update Object
		UserRoleUpdate userRoleUpdate=new UserRoleUpdate();
		User user=new User();
		UserDetails userdetail=new UserDetails();
		user.setId(user_id);
		RoleId roleId=new RoleId();
		roleId.setId(role_id);
		roleId.setRole(new_roleName);
		user.setRoleid(roleId);
		user.setFirstname((String) targetUser.get("firstname"));
		user.setLastname((String) targetUser.get("lastname"));
		user.setLocale((String) targetUser.get("locale"));
		user.setUserstatus((Integer) targetUser.get("userstatus"));
		userRoleUpdate.setUser(user);
		userdetail.setId((Integer) targetUser.get("userdetailid"));
		userdetail.setFullaccess("");
		userdetail.setTimezone((Integer) targetUser.get("timezone"));
		userdetail.setCurrencyid((Integer) targetUser.get("currencyid"));
		userRoleUpdate.setUserdetails(userdetail);

		Response response1=RestClient.doPost1("JSON",albatross_url,"users/{user_id}",authTokenMap,null,pathParamters,false,userRoleUpdate);
		if(!response1.jsonPath().getString("message_type").equals("is-success")){
			Assert.fail("User Role failed to update: "+response1.jsonPath().getString("message")+" "+response1.jsonPath().getString("message_type"));
		}
		else {
			Assert.assertTrue(true,"User Role Updated Successfully :"+response1.jsonPath().getString("message_type"));
		}
		Assert.assertEquals(response1.getStatusCode(),200);

		return response1;
	}
	
	// Method to update user role by role name (for backward compatibility)
	public Response updateRoleOfUserByRoleName(String albatross_url, Object privateApiToken, int role_id, String new_roleName, String current_roleName){
		// Handle role name mapping
		switch (current_roleName){
			case "TeamMember":
				current_roleName = "Team Member";
				break;
			case "RestrictedTeamMember":
				current_roleName = "Restricted Team Member";
				break;
			case "Admin":
				current_roleName = "Admin";
				break;
			case "AccountOwner":
			case "Owner":
				current_roleName = "Account Owner";
				break;
			default:
				break;
		}
		
		//Get User Details
		UsersGet usersGet=new UsersGet();
		Map<String, String> authTokenMap = getAuthTokenMap(privateApiToken);
		usersGet.setPage_size("5");
		Response response=RestClient.doPost("JSON",albatross_url,"users/get",authTokenMap,null,false,usersGet);
		JsonPath usersDetails=response.jsonPath();
		
		// Extract records array and find user by role dynamically
		List<Map<String, Object>> records = usersDetails.get("data.records");
		Map<String, Object> targetUser = null;
		
		// Loop through records to find user with matching role
		for (Map<String, Object> user : records) {
			String userRole = (String) user.get("role");
			if (current_roleName.equals(userRole)) {
				targetUser = user;
				break;
			}
		}
		
		// If no user found with the specified role, throw exception or handle gracefully
		if (targetUser == null) {
			throw new RuntimeException("No user found with role: " + current_roleName);
		}
		
		// Extract user details from the matched user
		int user_id = (Integer) targetUser.get("id");
		String parameter = String.valueOf(targetUser.get("id"));
		String user_role = (String) targetUser.get("role");
		
		//Update User Role
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("user_id", parameter);
		UserRoleUpdate userRoleUpdate=new UserRoleUpdate();
		User user=new User();
		UserDetails userdetail=new UserDetails();
		user.setId(user_id);
		RoleId roleId=new RoleId();
		roleId.setId(role_id);
		roleId.setRole(new_roleName);
		user.setRoleid(roleId);
		user.setFirstname((String) targetUser.get("firstname"));
		user.setLastname((String) targetUser.get("lastname"));
		user.setLocale((String) targetUser.get("locale"));
		user.setUserstatus((Integer) targetUser.get("userstatus"));
		userRoleUpdate.setUser(user);
		userdetail.setId((Integer) targetUser.get("userdetailid"));
		userdetail.setFullaccess("");
		userdetail.setTimezone((Integer) targetUser.get("timezone"));
		userdetail.setCurrencyid((Integer) targetUser.get("currencyid"));
		userRoleUpdate.setUserdetails(userdetail);

		Response response1=RestClient.doPost1("JSON",albatross_url,"users/{user_id}",authTokenMap,null,pathParamters,false,userRoleUpdate);
		if(!response1.jsonPath().getString("message_type").equals("is-success")){
			Assert.fail("User Role failed to update: "+response1.jsonPath().getString("message")+" "+response1.jsonPath().getString("message_type"));
		}
		else {
			Assert.assertTrue(true,"User Role Updated Successfully :"+response1.jsonPath().getString("message_type"));
		}
		Assert.assertEquals(response1.getStatusCode(),200);

		return response1;
	}

	private Map<String, String> getAuthTokenMap(Object authToken) {
		Map<String, String> authTokenMap = new HashMap<String, String>();
		if(authToken instanceof Map){
			authTokenMap = (Map<String, String>) authToken;
		}else {
			String apiKey = (String) authToken;
			authTokenMap = new HashMap<String, String>();
			authTokenMap.put("Authorization", "Bearer " + apiKey);
		}
		return authTokenMap;
	}

    private String getParamValue(Map<String, Object> params, String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue).toString();
    }

    private Map<String, String> buildEntityAccessMap(Map<String, Object> params) {
        Map<String, String> entityAccess = new HashMap<>();
        // Map param keys
        Map<String, String> keysMap = new HashMap<>();
        keysMap.put("candidates", "candidates");
        keysMap.put("contacts", "contacts");
        keysMap.put("jobs", "jobs");
        keysMap.put("companies", "companies");
        keysMap.put("deals", "deals");
        keysMap.put("placement_billing", "placementbilling");
        keysMap.put("task_meetings", "taskmeetings");
        keysMap.put("notes", "notes");
        keysMap.put("call_log", "calllog");
        keysMap.put("email_templates", "emailtemplates");
        keysMap.put("files", "files");

        Map<String, Integer> permissionCount = new HashMap<>();
        permissionCount.put("candidates", 6);
        permissionCount.put("contacts", 6);
        permissionCount.put("companies", 6);
        permissionCount.put("deals", 6);
        permissionCount.put("jobs", 6);
        permissionCount.put("placement_billing", 4);
        permissionCount.put("email_templates", 4);

        for (Map.Entry<String, String> entry : keysMap.entrySet()) {
            String paramKey = entry.getKey();
            String apiKey = entry.getValue();

            if (!params.containsKey(paramKey)) continue;

            String value = getParamValue(params, paramKey, null);
            if (value == null) continue;

            if (value.startsWith("[") && value.endsWith("]")) {
                int expected = permissionCount.getOrDefault(paramKey, 6);
                String[] parts = value.substring(1, value.length() - 1).split(",");

                if (parts.length != expected) {
                    throw new IllegalArgumentException("Invalid permission count for '" + paramKey +
                            "'. Expected " + expected + " but found " + parts.length);
                }

                entityAccess.put(apiKey + ".canview", parts[0].trim());
                entityAccess.put(apiKey + ".canadd", parts[1].trim());
                entityAccess.put(apiKey + ".canedit", parts[2].trim());
                entityAccess.put(apiKey + ".candelete", parts[3].trim());

                if (expected == 6) {
                    entityAccess.put(apiKey + ".ownerchange", parts[4].trim());
                    entityAccess.put(apiKey + ".fileaccess", parts[5].trim());
                }
            } else {
                // Shorthand: apply same value to all applicable fields
                entityAccess.put(apiKey + ".canview", value);
                entityAccess.put(apiKey + ".canadd", value);
                entityAccess.put(apiKey + ".canedit", value);
                entityAccess.put(apiKey + ".candelete", value);
                if (permissionCount.getOrDefault(paramKey, 6) == 6) {
                    entityAccess.put(apiKey + ".ownerchange", value);
                    entityAccess.put(apiKey + ".fileaccess", value);
                }
            }
        }
        return entityAccess;
    }

    private Map<String, Object> buildPermissionsMap(Map<String, Object> params) {
        Map<String, Object> permissions = new HashMap<>();
        for (String snakeKey : PERMISSION_KEYS) {
            String key = snakeKey.replaceAll("_", "");
            if(key.equals("recruiterperformancereportaccess") || key.equals("targetreportaccess")){
                permissions.put(key, getParamValue(params, snakeKey, "Everything"));
            } else {
                permissions.put(key, getParamValue(params, snakeKey, "1"));
            }
        }
        return permissions;
    }

    public void setupCustomRoleForEmailTemplateAccess(String ownerAlbatrossToken, String userRole, String appendToUserEmail, Map<String, Object> params, String albatrossURL) {
        Map<String, String> entityAccess = buildEntityAccessMap(params);
        Map<String, Object> fieldsPermission = buildPermissionsMap(params);

		userRole = userRole + String.valueOf(System.currentTimeMillis() % 10000);

        // Call API here using all collected data
        createCustomRole(albatrossURL, ownerAlbatrossToken, userRole, entityAccess, fieldsPermission);

        int roleId = getRoleId(albatrossURL, ownerAlbatrossToken, userRole);
        updateRoleOfUserByEmail(albatrossURL, ownerAlbatrossToken, roleId, userRole, appendToUserEmail);
    }

	public void addCollaboratorToDeal(String albatrossURL, Object privateApiToken, int collaboratorId, int collaboratorType, Response dealResponse) {
		Map<String, String> authTokenMap = getAuthTokenMap(privateApiToken);

		Map<String, String> dealInfo = extractDealInfoFromResponse(dealResponse);

		// Create deal update request using the updated CreateDeal POJO
        CreateDeal.Deal dealData = CreateDeal.Deal.builder()
                .id(Integer.parseInt(dealInfo.get("dealId")))
                .name(dealInfo.get("dealName"))
                .dealstage(Integer.parseInt(dealInfo.get("dealStage")))
                .dealvalue(dealInfo.get("dealValue"))
                .closedate(Long.parseLong(dealInfo.get("closeDate")))
                .slug(dealInfo.get("dealSlug"))
                .build();
                
        CreateDeal.SelectedOwner selectedOwner = CreateDeal.SelectedOwner.builder()
                .id(Integer.parseInt(dealInfo.get("ownerId")))
                .build();
                
        CreateDeal.SelectedDealType selectedDealType = CreateDeal.SelectedDealType.builder()
                .id(Integer.parseInt(dealInfo.get("dealType")))
                .build();
                
        CreateDeal.SelectedDealStage selectedDealStage = CreateDeal.SelectedDealStage.builder()
                .id(Integer.parseInt(dealInfo.get("dealStage")))
                .percentage("100")
                .build();

		CreateDeal.CollaboratorData collaboratorData = CreateDeal.CollaboratorData.builder()
			.id(collaboratorId)
			.type(collaboratorType)
			.build();
        
        CreateDeal deal = CreateDeal.builder()
                .deal(dealData)
                .selectedcandidates(new Object[]{})
                .selectedcompanies(new Object[]{})
                .selectedcontacts(new Object[]{})
                .selectedjobs(new Object[]{})
                .selectedOwner(selectedOwner)
                .selectedDealType(selectedDealType)
                .selectedDealStage(selectedDealStage)
                .collaboratorData(new CreateDeal.CollaboratorData[]{collaboratorData})
                .build();

		Response response = RestClient.doPost("JSON", albatrossURL, "deals/" + dealInfo.get("dealId"), authTokenMap, null, true, deal);
		response.then().statusCode(200);
	}

	public void addCollaboratorToJob(String albatrossURL, Object privateApiToken, int collaboratorId, int collaboratorType, Response jobResponse) {
		Map<String, String> authTokenMap = getAuthTokenMap(privateApiToken);

		Map<String, String> jobInfo = extractJobInfoFromResponse(jobResponse);

		// Create job update request using the updated UpdateJobRequest POJO
        JobUpdateData jobData = JobUpdateData.builder()
                .id(Integer.parseInt(jobInfo.get("jobId")))
                .slug(jobInfo.get("jobSlug"))
                .name(jobInfo.get("jobName"))
                .description(jobInfo.get("jobDescription"))
                .noofopenings(Integer.parseInt(jobInfo.get("noOfOpenings")))
                .qualificationid(0)
                .specialization("")
                .minexperienceinyears(0)
                .maxexperienceinyears(0)
                .annualsalarymin(0)
                .annualsalarymax(0)
                .salarytype("monthly")
                .job_type("parttime")
                .locality("")
                .city("")
                .country("")
                .postalcode(null)
                .state("")
                .address("")
                .currencyid(53)
                .companyid(Integer.parseInt(jobInfo.get("companyId")))
                .contactid(Integer.parseInt(jobInfo.get("contactId")))
                .details(null)
                .detailfilename(null)
                .allowapply(0)
                .jobcode(null)
                .showcompany(1)
                .showaccountname(0)
                .jobstatus(1)
                .jobstatuscomment("")
                .collaborator("")
                .ownerid(Integer.parseInt(jobInfo.get("ownerId")))
                .jobquestions("")
                .jdtext("Test Job Description")
                .job_category("")
                .job_skill("")
                .pay_rate(0)
                .bill_rate(0)
                .jobpostingstatus(0)
                .jobpostingdate(0)
                .hiring_pipeline_id(0)
                .mapped_pending_job_id(null)
                .build();

		UpdateJobRequest.Collaborator collaborator = UpdateJobRequest.Collaborator.builder()
			.user_ids(new Integer[]{collaboratorId})
			.team_ids(new Integer[]{})
			.build();
        
        UpdateJobRequest job = UpdateJobRequest.builder()
                .job(jobData)
                .address_changed(false)
                .filesInfo(new Object[]{})
                .deleteJobKey("")
                .secondaryContacts(new Object[]{})
                .xml_feeds(new Object[]{})
                .jobParserData(new Object[]{})
                .collaborator(collaborator)
                .build();

		Response response = RestClient.doPost("JSON", albatrossURL, "jobs/" + jobInfo.get("jobSlug"), authTokenMap, null, true, job);
		response.then().statusCode(200);
	}

	private Map<String, String> extractJobInfoFromResponse(Response jobResponse) {
		Map<String, String> jobInfo = new HashMap<>();
		try {
			JsonPath jsonPath = jobResponse.jsonPath();
			// Extract basic job information
			jobInfo.put("jobId", String.valueOf(jsonPath.getInt("data.job.id")));
			jobInfo.put("jobName", jsonPath.getString("data.job.name"));
			jobInfo.put("jobSlug", jsonPath.getString("data.job.slug"));
			jobInfo.put("jobDescription", jsonPath.getString("data.job.description"));
			jobInfo.put("noOfOpenings", String.valueOf(jsonPath.getInt("data.job.noofopenings")));
			jobInfo.put("ownerId", String.valueOf(jsonPath.getInt("data.job.ownerid")));
			jobInfo.put("companyId", String.valueOf(jsonPath.getInt("data.job.companyid")));
			jobInfo.put("contactId", String.valueOf(jsonPath.getInt("data.job.contactid")));
			jobInfo.put("jobStatus", String.valueOf(jsonPath.getInt("data.job.jobstatus")));
			jobInfo.put("createdBy", String.valueOf(jsonPath.getInt("data.job.createdby")));
			jobInfo.put("createdByName", jsonPath.getString("data.job.createdbyname"));
			jobInfo.put("updatedBy", String.valueOf(jsonPath.getInt("data.job.updatedby")));
			jobInfo.put("updatedByName", jsonPath.getString("data.job.updatedbyname"));
			jobInfo.put("createdOn", String.valueOf(jsonPath.getLong("data.job.createdon")));
			jobInfo.put("updatedOn", String.valueOf(jsonPath.getLong("data.job.updatedon")));
			jobInfo.put("collaborator", jsonPath.getString("data.job.collaborator"));
		} catch (Exception e) {
			System.err.println("Error extracting job info: " + e.getMessage());
		}
		return jobInfo;
	}

	private Map<String, String> extractDealInfoFromResponse(Response dealResponse) {
		Map<String, String> dealInfo = new HashMap<>();
		try {
			JsonPath jsonPath = dealResponse.jsonPath();
			// Extract basic deal information
			dealInfo.put("dealId", String.valueOf(jsonPath.getInt("data.deal.id")));
			dealInfo.put("dealName", jsonPath.getString("data.deal.name"));
			dealInfo.put("dealSlug", jsonPath.getString("data.deal.slug"));
			dealInfo.put("dealValue", jsonPath.getString("data.deal.dealvalue"));
			dealInfo.put("dealStage", String.valueOf(jsonPath.getInt("data.deal.dealstage")));
			dealInfo.put("dealStageLabel", jsonPath.getString("data.deal.dealstagelabel"));
			dealInfo.put("dealType", String.valueOf(jsonPath.getInt("data.deal.dealtype")));
			dealInfo.put("dealTypeLabel", jsonPath.getString("data.deal.dealtypelabel"));
			dealInfo.put("ownerId", String.valueOf(jsonPath.getInt("data.deal.ownerid")));
			dealInfo.put("ownerName", jsonPath.getString("data.deal.ownername"));
			dealInfo.put("createdBy", String.valueOf(jsonPath.getInt("data.deal.createdby")));
			dealInfo.put("createdByName", jsonPath.getString("data.deal.createdbyname"));
			dealInfo.put("updatedBy", String.valueOf(jsonPath.getInt("data.deal.updatedby")));
			dealInfo.put("updatedByName", jsonPath.getString("data.deal.updatedbyname"));
			dealInfo.put("createdOn", String.valueOf(jsonPath.getLong("data.deal.createdon")));
			dealInfo.put("updatedOn", String.valueOf(jsonPath.getLong("data.deal.updatedon")));
			dealInfo.put("closeDate", String.valueOf(jsonPath.getLong("data.deal.closedate")));
			dealInfo.put("collaborator", jsonPath.getString("data.deal.collaborator"));
		} catch (Exception e) {
			System.err.println("Error extracting deal info: " + e.getMessage());
		}
		return dealInfo;
	}

	public void updateUserTimezone(String albatross_url, Object authToken, int userId, int timezone) {
		Map<String, String> authTokenMap = getAuthTokenMap(authToken);

		UsersGet usersGet = new UsersGet();
		usersGet.setPage_size("10");
		Response response = RestClient.doPost("JSON", albatross_url, "users/get", authTokenMap, null, false, usersGet);
		JsonPath usersDetails = response.jsonPath();

		List<Map<String, Object>> records = usersDetails.get("data.records");

		if (records == null || records.isEmpty()) {
			return;
		}

		Map<String, Object> targetUser = null;
		for (Map<String, Object> user : records) {
			if (((Integer) user.get("id")).intValue() == userId) {
				targetUser = user;
				break;
			}
		}

		if (targetUser == null) {
			return;
		}

		JSONObject currentUser = new JSONObject();
		currentUser.put("id", targetUser.get("id"));
		currentUser.put("firstname", targetUser.get("firstname"));
		currentUser.put("lastname", targetUser.get("lastname"));
		currentUser.put("email", targetUser.get("email"));
		currentUser.put("contactnumber", targetUser.get("contactnumber") != null ? targetUser.get("contactnumber") : "");
		currentUser.put("city", targetUser.get("city") != null ? targetUser.get("city") : "");
		currentUser.put("country", targetUser.get("country") != null ? targetUser.get("country") : "");
		currentUser.put("state", targetUser.get("state") != null ? targetUser.get("state") : "");
		currentUser.put("locale", targetUser.get("locale") != null ? targetUser.get("locale") : "");

		JSONObject currentUserDetails = new JSONObject();
		currentUserDetails.put("id", targetUser.get("userdetailid"));
		currentUserDetails.put("timezone", targetUser.get("timezone"));
		currentUserDetails.put("currencyid", targetUser.get("currencyid"));
		currentUserDetails.put("time_format_type", timezone);

		JSONObject payload = new JSONObject();
		payload.put("current_user", currentUser);
		payload.put("current_user_details", currentUserDetails);

		Response updateResponse = RestClient.doPost("JSON", albatross_url, "users/update-profile/" + userId, authTokenMap, null, true, payload);

		if (!updateResponse.jsonPath().getString("message_type").equals("is-success")) {
			throw new RuntimeException("Failed to update user " + userId + " timezone: " +
					updateResponse.jsonPath().getString("message"));
		}
	}
}