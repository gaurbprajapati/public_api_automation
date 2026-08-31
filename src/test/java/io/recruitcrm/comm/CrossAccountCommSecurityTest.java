package io.recruitcrm.comm;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.pojo.comm.AssignNumber;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CrossAccountCommSecurityTest extends TestBase {

	private int phoneNumberIdA, phoneNumberIdB;
	private int assignedPhoneNumberIdA, assignedPhoneNumberIdB;
	private int userId1A, userId1B, userId2A, userId2B;
	private final AllCrudFunctions albatrossFunctions = new AllCrudFunctions();

	@BeforeClass(alwaysRun = true)	public void setupTestData() {
		setupTestDataForAccountA();
		setupTestDataForAccountB();
	}
	
	private void setupTestDataForAccountA() {
		ReaperIntegration.updateTwilioSubaccount(accountA.getAccountId());

		Response getUsers = albatrossFunctions.getUsers(albatrossURL, accountA_Token);
		JsonPath user = getUsers.jsonPath();
		userId1A = user.get("data.records[0].id");
		userId2A = user.get("data.records[1].id");

		ReaperIntegration.insertPurchasedNumber(accountA.getAccountId(), userId1A);
		phoneNumberIdA = albatrossFunctions.getPurchasedPhoneNumberId(commURL, accountA_Token).jsonPath().get("phone_numbers[0].id");
		assignedPhoneNumberIdA = albatrossFunctions.assignPhoneNumber(commURL, accountA_Token, userId1A, phoneNumberIdA).jsonPath().get("data.id");
	}
	
	private void setupTestDataForAccountB() {
		ReaperIntegration.updateTwilioSubaccount(accountB.getAccountId());

		Response getUsers = albatrossFunctions.getUsers(albatrossURL, accountB_Token);
		JsonPath user = getUsers.jsonPath();
		userId1B = user.get("data.records[0].id");
		userId2B = user.get("data.records[1].id");

		ReaperIntegration.insertPurchasedNumber(accountB.getAccountId(), userId1B);
		phoneNumberIdB = albatrossFunctions.getPurchasedPhoneNumberId(commURL, accountB_Token).jsonPath().get("phone_numbers[0].id");
		assignedPhoneNumberIdB = albatrossFunctions.assignPhoneNumber(commURL, accountB_Token, userId1B, phoneNumberIdB).jsonPath().get("data.id");
	}
	
	@Owner("Harika")
	@Test(dataProvider = "crossAccountCommTestData", groups = "nightly-build")
	public void crossAccountCommOperations_Test(String testScenario, String accountType, String tokenType, 
			String operation, String expectedStatusCode, String expectedResponse, String description) {
		
		String token = getTokenForAccount(accountType, tokenType);
		Response response = null;
		
		try {
			switch (operation.toUpperCase()) {
					
				case "GET_PURCHASED_PHONE_NUMBERS":
					response = RestClient.doGet("JSON", commURL, "phone-numbers/purchased", token, null, null, true);
					break;
					
				case "POST_ASSIGN_PHONE_NUMBER":
					// Handle cross-account assignment scenarios
					int targetUserId = testScenario.contains("USERB") ? userId1B : userId1A;
					int targetPhoneNumberId = testScenario.contains("PHONENUMBERB") ? phoneNumberIdB : phoneNumberIdA;
					
					if (targetPhoneNumberId != 0) {
						String availabilityString = getAvailabilityString();

						String basePath = "phone-numbers/assign";

						AssignNumber assignPhoneNumber = new AssignNumber();
						assignPhoneNumber.setUser_id(String.valueOf(targetUserId));
						assignPhoneNumber.setPhone_number_id(String.valueOf(targetPhoneNumberId));
						assignPhoneNumber.setNumber_title("Owner Phone Number");
						assignPhoneNumber.setVoice_reply("The person you are calling is not available");
						assignPhoneNumber.setMasked_number("+1234567890");
						assignPhoneNumber.setAvailability(availabilityString);

						response = RestClient.doPost("JSON", commURL, basePath, token, null, true, assignPhoneNumber);
					
					} else {
						// Skip test if required data not available
						return;
					}
					break;
					
				case "PUT_EDIT_ASSIGN_PHONE_NUMBER":
					// Handle cross-account edit scenarios
					int editUserId = testScenario.contains("USERB") ? userId2B : userId2A;
					int editAssignmentId = testScenario.contains("ASSIGNMENTB") ? assignedPhoneNumberIdB : assignedPhoneNumberIdA;
					
					if (editAssignmentId != 0) {
						String availabilityString = getAvailabilityString();

						String basePath = "phone-numbers/assign/{id}";

						Map<String, String> pathParamters = new HashMap<>();
						pathParamters.put("id", String.valueOf(editAssignmentId));

						AssignNumber assignPhoneNumber = new AssignNumber();
						assignPhoneNumber.setUser_id(String.valueOf(editUserId));
						assignPhoneNumber.setNumber_title("Reassign Phone Number to Admin");
						assignPhoneNumber.setVoice_reply("The person you are calling is not available");
						assignPhoneNumber.setMasked_number("+1234567890");
						assignPhoneNumber.setAvailability(availabilityString);

						response = RestClient.doPut1("JSON", commURL, basePath, token, null, pathParamters, true, assignPhoneNumber);
		
					} else {
						// Skip test if required data not available
						return;
					}
					break;
					
				case "DELETE_UNASSIGN_PHONE_NUMBER":
					// Handle cross-account unassign scenarios
					int unassignAssignmentId = testScenario.contains("ASSIGNMENTB") ? assignedPhoneNumberIdB : assignedPhoneNumberIdA;
					
					if (unassignAssignmentId != 0) {
						String basePath = "phone-numbers/assign/{id}";

						Map<String, String> pathParamters = new HashMap<>();
						pathParamters.put("id", String.valueOf(unassignAssignmentId));

						response = RestClient.doDelete("JSON", commURL, basePath, token, null, pathParamters, true);
					} else {
						// Skip test if required data not available
						return;
					}
					break;
					
				case "GET_UNASSIGNED_USERS":
					response = RestClient.doGet("JSON", commURL, "phone-numbers/unassigned-userids", token, null, null, true);
					break;
					
				default:
					Assert.fail("Unknown operation: " + operation);
					return;
			}
			
			// Validate response based on expected status code
			Assert.assertEquals(response.getStatusCode(), Integer.parseInt(expectedStatusCode), 
					"Test scenario: " + testScenario + " - " + description);

			System.out.println(response.prettyPrint());
			
			// Additional validations for successful responses
			if (expectedStatusCode.equals("200")) {
				response.then().body(Matchers.notNullValue());
				
				// Specific validations based on operation
				switch (operation.toUpperCase()) {
						
					case "GET_PURCHASED_PHONE_NUMBERS":
						response.then().body("phone_numbers", Matchers.notNullValue());
						
						// Verify data isolation - Account A should not see Account B's phone numbers and vice versa
						if (accountType.equals("AccountA")) {
							// Account A should only see its own phone numbers
							response.then().body("phone_numbers", Matchers.not(Matchers.hasItem(
								Matchers.hasEntry("id", phoneNumberIdB)
							)));
							response.then().body("phone_numbers", Matchers.not(Matchers.hasItem(
								Matchers.hasEntry("user_id", userId1B)
							)));
						} else if (accountType.equals("AccountB")) {
							// Account B should only see its own phone numbers
							response.then().body("phone_numbers", Matchers.not(Matchers.hasItem(
								Matchers.hasEntry("id", phoneNumberIdA)
							)));
							response.then().body("phone_numbers", Matchers.not(Matchers.hasItem(
								Matchers.hasEntry("user_id", userId1A)
							)));
						}
						break;
						
					case "GET_UNASSIGNED_USERS":
						response.then().body("user_ids", Matchers.notNullValue());
						
						// Verify data isolation - Account A should not see Account B's users and vice versa
						if (accountType.equals("AccountA")) {
							// Account A should only see its own users
							response.then().body("user_ids", Matchers.not(Matchers.hasItem(userId2B)));
						} else if (accountType.equals("AccountB")) {
							// Account B should only see its own users
							response.then().body("user_ids", Matchers.not(Matchers.hasItem(userId2A)));
						}
						break;

					case "POST_ASSIGN_PHONE_NUMBER":
						if (accountType.equals("AccountA") && testScenario.contains("USERB")) {
							Assert.assertEquals(response.jsonPath().get("user_id[0]"), "The selected user id is invalid.");
						}
						if (accountType.equals("AccountA") && testScenario.contains("PHONENUMBERB")) {
							Assert.assertEquals(response.jsonPath().get("phone_number_id[0]"), "The selected phone number id is invalid.");
						}
						break;

					case "PUT_EDIT_ASSIGN_PHONE_NUMBER":
						if (accountType.equals("AccountA") && testScenario.contains("USERB")) {
							Assert.assertEquals(response.jsonPath().get("user_id[0]"), "The selected user id is invalid.");
						}
						if (accountType.equals("AccountA") && testScenario.contains("ASSIGNMENTB") && !testScenario.contains("USERB")) {
							Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
							Assert.assertEquals(response.jsonPath().get("status"), "fail");
							Assert.assertEquals(response.jsonPath().get("message"), "Phone Number not found");
						}
						break;

					case "DELETE_UNASSIGN_PHONE_NUMBER":
						if (accountType.equals("AccountA") && testScenario.contains("ASSIGNMENTB")) {
							Assert.assertEquals(response.jsonPath().get("message_type"), "is-danger");
							Assert.assertEquals(response.jsonPath().get("message"), "Assigned Number not found");
							Assert.assertEquals(response.jsonPath().get("status"), "fail");
						}
						break;
				}
			}
			
		} catch (Exception e) {
			// Handle exceptions based on expected status code
			if (expectedStatusCode.equals("500") || expectedStatusCode.equals("503")) {
				// Expected server error
				return;
			} else {
				throw e;
			}
		}
	}

	@DataProvider(name = "crossAccountCommTestData")
	public static Object[][] crossAccountCommTestData() {
		return new Object[][] {
			{"SCENARIO_GET_PURCHASED_PHONE_NUMBERS_ISOLATION_A", "AccountA", "valid", "GET_PURCHASED_PHONE_NUMBERS", "200", "success", "Account A should only see its own purchased numbers"},
			{"SCENARIO_GET_UNASSIGNED_USERS_ISOLATION_A", "AccountA", "valid", "GET_UNASSIGNED_USERS", "200", "success", "Account A should only see its own unassigned users"},

			 // Account A trying to assign Account B's phone number to Account B's user (should fail)
			 {"SCENARIO_ASSIGN_TOKENA_PHONENUMBERB_USERB", "AccountA", "valid", "POST_ASSIGN_PHONE_NUMBER", "422", "forbidden", "Account A cannot assign Account B's phone number to Account B's user"},
			
			 // Account A trying to assign Account A's phone number to Account B's user (should fail)
			 {"SCENARIO_ASSIGN_TOKENA_PHONENUMBERA_USERB", "AccountA", "valid", "POST_ASSIGN_PHONE_NUMBER", "422", "forbidden", "Account A cannot assign phone number to Account B's user"},
			
			 // Account A trying to assign Account B's phone number to Account A's user (should fail)
			 {"SCENARIO_ASSIGN_TOKENA_PHONENUMBERB_USERA", "AccountA", "valid", "POST_ASSIGN_PHONE_NUMBER", "422", "forbidden", "Account A cannot assign Account B's phone number to Account A's user"},
			
			 // Account A trying to edit Account B's assignment (should fail)
			 {"SCENARIO_EDIT_TOKENA_USERB_ASSIGNMENTB", "AccountA", "valid", "PUT_EDIT_ASSIGN_PHONE_NUMBER", "422", "forbidden", "Account A cannot edit Account B's assignment"},
			
			 // Account A trying to edit Account A's assignment with Account B's user (should fail)
			 {"SCENARIO_EDIT_TOKENA_USERB_ASSIGNMENTA", "AccountA", "valid", "PUT_EDIT_ASSIGN_PHONE_NUMBER", "422", "forbidden", "Account A cannot edit assignment with Account B's user"},
			
			 // Account A trying to edit Account B's assignment with Account A's phone number (should fail)
			 {"SCENARIO_EDIT_TOKENA_USERA_ASSIGNMENTB", "AccountA", "valid", "PUT_EDIT_ASSIGN_PHONE_NUMBER", "200", "forbidden", "Account A cannot edit assignment with Account B's phone number"},
			
			 // Account A trying to unassign Account B's assignment (should fail)
			 {"SCENARIO_UNASSIGN_TOKENA_ASSIGNMENTB", "AccountA", "valid", "DELETE_UNASSIGN_PHONE_NUMBER", "200", "forbidden", "Account A cannot unassign Account B's assignment"},
		
		};
	}

	private static String getAvailabilityString() {
		try {
			Map<String, Object> startTime = new HashMap<>();
			startTime.put("id", "06:00 AM");
			startTime.put("value", 21600);

			Map<String, Object> endTime = new HashMap<>();
			endTime.put("id", "06:00 PM");
			endTime.put("value", 64800);

			Map<String, String> days = new HashMap<>();
			days.put("id", "Mon - Fri");
			days.put("value", "0,1,2,3,4");

			Map<String, Object> availabilityMap = new HashMap<>();
			availabilityMap.put("start_time", startTime);
			availabilityMap.put("end_time", endTime);
			availabilityMap.put("days", days);

			List<Map<String, Object>> availability = Collections.singletonList(availabilityMap);

			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(availability);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "[]"; // Return an empty JSON array as a fallback
		}
	}
} 