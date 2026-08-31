package io.recruitcrm.hiringPipeline;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.*;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.annotations.*;

import io.rcrm.api.javafaker.hiringPipeline.HiringPipeline;
import io.rcrm.api.pojo.albatross.hiringpipeline.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACHiringPipelineServiceTest extends TestBase {

	private static final String ROLE_WITH_ACCESS = "Admin";
	private static final String ROLE_WITHOUT_ACCESS = "CustomRoleNothing";

	private static final String SUCCESS = "Success";
	private static final String NO_PERMISSION = "NoPermission";
	private static final String ACCESS_CONTROL_ERROR_MESSAGE = "You do not have permission to perform this action.";

	private Map<String, String> albatrossTknMap;
	private Map<String, Integer> userIdsMap;
	private int hiringPipelineId;

	HiringPipeline faker = new HiringPipeline();

	@BeforeClass
	public void setup() {
		albatrossTknMap = new HashMap<>();
		userIdsMap = new HashMap<>();
		setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "rbacHiringPipelineServiceData", groups = "rbac")
	public void hiringPipelineRBAC_Service_Test(String operation, String executorRole, int expectedStatusCode, String expectedOutcome, String testDescription) {
		String executorToken = albatrossTknMap.get(executorRole);
		Response response = null;

		switch (operation) {
		case "POST_ADD": {
			CreateHiringPipeline body = new CreateHiringPipeline();
			body.setName(faker.getHiringPipelineName());
			body.setIs_primary("0");
			body.setHiring_stages(getHiringStagesList());
			response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/add", executorToken, null, true, body);
			if (response.getStatusCode() == 200) {
				JsonPath jp = response.jsonPath();
				hiringPipelineId = jp.get("id");
			}
			break;
		}
		case "GET_LIST":
			response = RestClient.doGet("JSON", hiringPipelineServiceURL, "pipelines/list", executorToken, null, null, true);
			break;
		case "GET_BY_ID": {
			Map<String, String> path = new HashMap<>();
			path.put("ID", String.valueOf(hiringPipelineId));
			response = RestClient.doGet("JSON", hiringPipelineServiceURL, "pipelines/{ID}", executorToken, null, path, true);
			break;
		}
		case "POST_UPDATE": {
			Map<String, String> path = new HashMap<>();
			path.put("ID", String.valueOf(hiringPipelineId));
			CreateHiringPipeline updateBody = new CreateHiringPipeline();
			updateBody.setName("Updated " + faker.getHiringPipelineName());
			updateBody.setIs_primary("0");
			updateBody.setHiring_stages(getHiringStagesList());
			response = RestClient.doPost1("JSON", hiringPipelineServiceURL, "pipelines/update/{ID}", executorToken, null, path, true, updateBody);
			break;
		}
		case "DELETE_PIPELINE": {
			Map<String, String> path = new HashMap<>();
			path.put("ID", String.valueOf(hiringPipelineId));
			response = RestClient.doDelete("JSON", hiringPipelineServiceURL, "pipelines/delete/{ID}", executorToken, null, path, false);
			break;
		}
		case "POST_MARK_PRIMARY": {
			JSONObject markPrimary = new JSONObject();
			markPrimary.put("pipeline_id", hiringPipelineId);
			response = RestClient.doPost("JSON", hiringPipelineServiceURL, "pipelines/mark-primary", executorToken, null, true, markPrimary);
			break;
		}
		default:
			throw new IllegalArgumentException("Unsupported operation: " + operation);
		}

		validateResponse(response, expectedStatusCode, expectedOutcome, testDescription, operation);
	}

	private void validateResponse(Response response, int expectedStatusCode, String expectedOutcome, String testDescription, String operation) {
		int actual = response.getStatusCode();
		if (NO_PERMISSION.equals(expectedOutcome)) {
			assertThat("Test Case FAILED: " + testDescription + " - expected status " + expectedStatusCode + ", got: " + actual, actual, equalTo(expectedStatusCode));
			JsonPath jp = response.jsonPath();
			assertThat("Test Case FAILED: " + testDescription + " - expected error: true", jp.getBoolean("error"), equalTo(true));
			assertThat("Test Case FAILED: " + testDescription + " - expected error_code: " + expectedStatusCode, jp.getInt("error_code"), equalTo(expectedStatusCode));
			assertThat("Test Case FAILED: " + testDescription + " - expected error_message", jp.getString("error_message"), equalTo(ACCESS_CONTROL_ERROR_MESSAGE));
		} else {
			response.then().statusCode(expectedStatusCode);
			if (expectedStatusCode == 200 && SUCCESS.equals(expectedOutcome)) {
				if ("POST_ADD".equals(operation))
					response.then().body("id", Matchers.notNullValue());
				else if ("GET_LIST".equals(operation) || "GET_BY_ID".equals(operation))
					assertThat("Response should have a body for " + testDescription, response.getBody().asString(), not(emptyOrNullString()));
			}
		}
	}

	@DataProvider(name = "rbacHiringPipelineServiceData")
	public Object[][] rbacHiringPipelineServiceData() {
		List<Object[]> rows = new ArrayList<>();
		String[] withAccess = { "GET_LIST", "GET_BY_ID" };
		for (String accessOp : withAccess) {
			rows.add(new Object[] { accessOp, ROLE_WITH_ACCESS, 200, SUCCESS, accessOp + " — " + ROLE_WITH_ACCESS });
			rows.add(new Object[] { accessOp, ROLE_WITHOUT_ACCESS, 200, SUCCESS, accessOp + " — " + ROLE_WITHOUT_ACCESS });
		}
		String[] withoutAccess = { "POST_ADD", "POST_UPDATE", "POST_MARK_PRIMARY", "DELETE_PIPELINE" };
		for (String withoutAccessOp : withoutAccess) {
			rows.add(new Object[] { withoutAccessOp, ROLE_WITHOUT_ACCESS, 403, NO_PERMISSION, withoutAccessOp + " — " + ROLE_WITHOUT_ACCESS + " without toggle" });
			rows.add(new Object[] { withoutAccessOp, ROLE_WITH_ACCESS, 200, SUCCESS, withoutAccessOp + " — " + ROLE_WITH_ACCESS + " has toggle" });
		}
		return rows.toArray(new Object[0][]);
	}

	private ArrayList<Object> getHiringStagesList() {
		ArrayList<Object> hiringStagesList = new ArrayList<>();
		
		HiringStages stage1 = new HiringStages(10, 0);
		HiringStages stage2 = new HiringStages(1, 1);
		HiringStages stage3 = new HiringStages(8, 55);
		
		hiringStagesList.add(stage1);
		hiringStagesList.add(stage2);
		hiringStagesList.add(stage3);
		
		return hiringStagesList;
	}
}