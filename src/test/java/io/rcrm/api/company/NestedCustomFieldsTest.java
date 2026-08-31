package io.rcrm.api.company;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NestedCustomFieldsTest extends TestBase {

	commanFunction function = new commanFunction();
	JavaFakerCustomField faker = new JavaFakerCustomField();
	private String apiAuthToken;
	private String albatrossAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCandidateNestedCustomFields_Test() {
		Map<String, Object> fieldData = createNestedCustomDependency("candidate", "5");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity_type", "candidates");

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("1.field_name"), fieldData.get("parentFieldName"));
		Assert.assertEquals(jsonPath.get("1.field_type"), "dropdown");
		Assert.assertEquals(jsonPath.get("1.children.1.field_name"), fieldData.get("childFieldName"));
		Assert.assertEquals(jsonPath.get("1.children.1.field_type"), "multiselect");
		Map<String, String> dependencyMap = jsonPath.getMap("1.children.1.dependency");
		Assert.assertEquals(dependencyMap.get(fieldData.get("option1")),
				fieldData.get("option1") + ", " + fieldData.get("option2"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCompanyNestedCustomFields_Test() {
		Map<String, Object> fieldData = createNestedCustomDependency("company", "3");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity_type", "companies");

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("1.field_name"), fieldData.get("parentFieldName"));
		Assert.assertEquals(jsonPath.get("1.field_type"), "dropdown");
		Assert.assertEquals(jsonPath.get("1.children.1.field_name"), fieldData.get("childFieldName"));
		Assert.assertEquals(jsonPath.get("1.children.1.field_type"), "multiselect");
		Map<String, String> dependencyMap = jsonPath.getMap("1.children.1.dependency");
		Assert.assertEquals(dependencyMap.get(fieldData.get("option1")),
				fieldData.get("option1") + ", " + fieldData.get("option2"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getContactNestedCustomFields_Test() {

		Map<String, Object> fieldData = createNestedCustomDependency("contact", "2");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity_type", "contacts");

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("1.field_name"), fieldData.get("parentFieldName"));
		Assert.assertEquals(jsonPath.get("1.field_type"), "dropdown");

		Assert.assertEquals(jsonPath.get("1.children.1.field_name"), fieldData.get("childFieldName"));
		Assert.assertEquals(jsonPath.get("1.children.1.field_type"), "multiselect");

		Map<String, String> dependencyMap = jsonPath.getMap("1.children.1.dependency");
		Assert.assertEquals(dependencyMap.get(fieldData.get("option1")),
				fieldData.get("option1") + ", " + fieldData.get("option2"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getJobNestedCustomFields_Test() {
		Map<String, Object> fieldData = createNestedCustomDependency("job", "4");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity_type", "jobs");

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("1.field_name"), fieldData.get("parentFieldName"));
		Assert.assertEquals(jsonPath.get("1.field_type"), "dropdown");
		Assert.assertEquals(jsonPath.get("1.children.1.field_name"), fieldData.get("childFieldName"));
		Assert.assertEquals(jsonPath.get("1.children.1.field_type"), "multiselect");
		Map<String, String> dependencyMap = jsonPath.getMap("1.children.1.dependency");
		Assert.assertEquals(dependencyMap.get(fieldData.get("option1")),
				fieldData.get("option1") + ", " + fieldData.get("option2"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getDealNestedCustomFields_Test() {

		Map<String, Object> fieldData = createNestedCustomDependency("deal", "11");

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entity_type", "deals");

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, queryParameters,
				null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("1.field_name"), fieldData.get("parentFieldName"));
		Assert.assertEquals(jsonPath.get("1.field_type"), "dropdown");

		Assert.assertEquals(jsonPath.get("1.children.1.field_name"), fieldData.get("childFieldName"));
		Assert.assertEquals(jsonPath.get("1.children.1.field_type"), "multiselect");

		Map<String, String> dependencyMap = jsonPath.getMap("1.children.1.dependency");
		Assert.assertEquals(dependencyMap.get(fieldData.get("option1")),
				fieldData.get("option1") + ", " + fieldData.get("option2"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getAllNestedCustomFields_Test() {

		Map<String, Object> candidateFieldData = createNestedCustomDependency("candidate", "5");
		Map<String, Object> companyFieldData = createNestedCustomDependency("company", "3");
		Map<String, Object> contactFieldData = createNestedCustomDependency("contact", "2");
		Map<String, Object> jobFieldData = createNestedCustomDependency("job", "4");
		Map<String, Object> dealFieldData = createNestedCustomDependency("deal", "11");

		List<Map<String, Object>> testData = Arrays.asList(candidateFieldData, companyFieldData, contactFieldData,
				jobFieldData, dealFieldData);

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath jsonPath = response.jsonPath();

		for (int i = 0; i < testData.size(); i++) {
			int index = i + 1;

			Assert.assertNotNull(jsonPath.get(index + ".field_name"));
			Assert.assertEquals(jsonPath.get(index + ".field_type"), "dropdown");

			Assert.assertNotNull(jsonPath.get(index + ".children.1.field_name"));
			Assert.assertEquals(jsonPath.get(index + ".children.1.field_type"), "multiselect");

			Assert.assertNotNull(jsonPath.get(index + ".children.1.dependency"),
					"Dependency is null at index " + index);
		}
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getEmptyStateNestedCustomFields_Test() {

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertTrue(jsonPath.getList("$").isEmpty(), "Response is not empty");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetNestedCustomFields_Test() {

		Response response = RestClient.doGet("JSON", baseURL, "nested-custom-fields", apiAuthToken + "123", null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		JsonPath jsonPath = response.jsonPath();

		Assert.assertEquals(jsonPath.get("error"), "Unauthorized");
	}

	public Map<String, Object> createNestedCustomDependency(String entityType, String dependencyType) {
		Map<String, Object> fieldData = new HashMap<>();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();

		String option1 = faker.getNumberOfDefaultOptionsValues(1);
		String option2 = faker.getNumberOfDefaultOptionsValues(1);
		String option3 = faker.getNumberOfDefaultOptionsValues(1);
		String parentFieldName = faker.getRandomCustomFieldName();
		String childFieldName = faker.getRandomCustomFieldName();

		Response parentResponse = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, entityType,
				parentFieldName, "dropdown", option1 + "," + option2 + "," + option3);
		JsonPath parentJson = parentResponse.jsonPath();
		int parentCustomFieldId = parentJson.get("data.custumField.id");
		int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

		Response childResponse = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, entityType,
				childFieldName, "multiselect", option1 + "," + option2 + "," + option3);
		JsonPath childJson = childResponse.jsonPath();
		int childCustomFieldId = childJson.get("data.custumField.id");
		int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
		int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

		function.createNestedDependency(albatrossURL, albatrossAuthToken, dependencyType, parentCustomFieldId,
				childCustomFieldId, parentOptionId, childOptionId1, childOptionId2);

		fieldData.put("option1", option1);
		fieldData.put("option2", option2);
		fieldData.put("parentFieldName", parentFieldName);
		fieldData.put("childFieldName", childFieldName);

		return fieldData;
	}

}