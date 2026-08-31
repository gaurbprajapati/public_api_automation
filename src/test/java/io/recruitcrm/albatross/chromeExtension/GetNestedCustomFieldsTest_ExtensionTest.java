package io.recruitcrm.albatross.chromeExtension;

import java.lang.reflect.Method;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetNestedCustomFieldsTest_ExtensionTest extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "extensions/chrome/nested-custom-fields/get/{entityTypeId}";
	commanFunction function = new commanFunction();
    JavaFakerCustomField faker = new JavaFakerCustomField();
    
	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
    public void getCandidateNestedCustomFields_Test(Map<String, Object> candidateFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");

		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("NestedCustomFields Dependency Data"));
		assertNestedCustomFieldIds(response, candidateFieldData);
    }

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCompanyNestedCustomFields_Test(Map<String, Object> companyFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("3");

		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("NestedCustomFields Dependency Data"));
		assertNestedCustomFieldIds(response, companyFieldData);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getContactNestedCustomFields_Test(Map<String, Object> contactFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");

		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("NestedCustomFields Dependency Data"));
		assertNestedCustomFieldIds(response, contactFieldData);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCandidateNestedCustomFieldsUnauthorized_Test(Map<String, Object> candidateFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-danger"));
		assertThat(response.jsonPath().getString("message"), is("Unauthorized access"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCandidateNestedCustomFieldsEmptyToken_Test(Map<String, Object> companyFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, "", null, pathParamters, true);
		assertThat(response.getStatusCode(), is(401));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getNestedCustomFieldsInvalidEntityTypeId_Test() {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("invalid");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(422));
		assertThat(response.jsonPath().getString("message_type"), is("is-danger"));
		assertThat(response.jsonPath().getString("message"), is("The entity type id must be an integer."));
		assertThat(response.jsonPath().getList("data").isEmpty(), is(true));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getNestedCustomFieldsEmptyEntityTypeId_Test() {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknA, null, pathParamters, true);
		assertThat(response.getStatusCode(), is(404));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCandidateNestedCustomFieldsCrossAccount_Test(Map<String, Object> candidateFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, albatrossTknB, null, pathParamters, true);
		validateEmptyDataResponse(response);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCandidateNestedCustomFieldsAdmin_Test(Map<String, Object> candidateFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("5");
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, adminToken, null, pathParamters, true);
		assertNestedCustomFieldIds(response, candidateFieldData);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getCompanyNestedCustomFieldsTeamMember_Test(Map<String, Object> companyFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("3");
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, teamMemberToken, null, pathParamters, true);
		assertNestedCustomFieldIds(response, companyFieldData);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createNestedCustomDependency", groups = "nightly-build")
	public void getContactNestedCustomFieldsTeamMember_Test(Map<String, Object> contactFieldData) {
		Map<String, String> pathParamters = createEntityTypeIdPathParameters("2");
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doGetExtension1("JSON", albatrossURL, basePath, teamMemberToken, null, pathParamters, true);
		assertNestedCustomFieldIds(response, contactFieldData);
	}

	private void validateEmptyDataResponse(Response response) {
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("message"), is("NestedCustomFields Dependency Data"));
		assertThat(response.jsonPath().getList("data").isEmpty(), is(true));
	}

	private void assertNestedCustomFieldIds(Response response, Map<String, Object> fieldData) {
		String parentCustomFieldId = fieldData.get("parentCustomFieldId").toString();
		String childCustomFieldId = fieldData.get("childCustomFieldId").toString();
		String parentOptionId = fieldData.get("parentOptionId").toString();
		String childOptionId1 = fieldData.get("childOptionId1").toString();
		String childOptionId2 = fieldData.get("childOptionId2").toString();

		assertThat(response.jsonPath().getMap("data[0]").containsKey(parentCustomFieldId), is(true));
		String childrenPath = "data[0]." + parentCustomFieldId + ".children";
		assertThat(response.jsonPath().getMap(childrenPath).containsKey(childCustomFieldId), is(true));
		String dependencyPath = "data[0]." + parentCustomFieldId + ".children." + childCustomFieldId + ".dependency";
		assertThat(response.jsonPath().getMap(dependencyPath).containsKey(parentOptionId), is(true));
		String dependencyArrayPath = "data[0]." + parentCustomFieldId + ".children." + childCustomFieldId + ".dependency." + parentOptionId;
		List<String> childOptions = response.jsonPath().getList(dependencyArrayPath, String.class);
		assertThat(childOptions, hasItems(childOptionId1, childOptionId2));
		assertThat(childOptions.size(), is(2));
	}

	private Map<String, String> createEntityTypeIdPathParameters(String entityTypeId) {
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("entityTypeId", entityTypeId);
        return pathParameters;
    }

	@DataProvider
    public Object[][] createNestedCustomDependency(Method method) {
		String entityType;
		String dependencyType;
		
		String methodName = method.getName();
		if (methodName.contains("Candidate")) {
			entityType = "candidate";
			dependencyType = "5";
		} else if (methodName.contains("Company")) {
			entityType = "company";
			dependencyType = "3";
		} else if (methodName.contains("Contact")) {
			entityType = "contact";
			dependencyType = "2";
		} else {
			throw new IllegalArgumentException("Unknown entity type for method: " + methodName);
		}
		
		Map<String, Object> fieldData = new HashMap<>();

		String option1 = faker.getNumberOfDefaultOptionsValues(1);
		String option2 = faker.getNumberOfDefaultOptionsValues(1);
		String option3 = faker.getNumberOfDefaultOptionsValues(1);
		String parentFieldName = faker.getRandomCustomFieldName();
		String childFieldName = faker.getRandomCustomFieldName();

		Response parentResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, entityType, parentFieldName, "dropdown", option1 + "," + option2 + "," + option3);
		JsonPath parentJson = parentResponse.jsonPath();
		int parentCustomFieldId = parentJson.get("data.custumField.id");
		int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

		Response childResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, entityType, childFieldName, "multiselect", option1 + "," + option2 + "," + option3);
		JsonPath childJson = childResponse.jsonPath();
		int childCustomFieldId = childJson.get("data.custumField.id");
		int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
		int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

		function.createNestedDependency(albatrossURL, albatrossTknA, dependencyType, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2);

		fieldData.put("option1", option1);
		fieldData.put("option2", option2);
		fieldData.put("parentFieldName", parentFieldName);
		fieldData.put("childFieldName", childFieldName);
		fieldData.put("parentCustomFieldId", parentCustomFieldId);
		fieldData.put("parentOptionId", parentOptionId);
		fieldData.put("childCustomFieldId", childCustomFieldId);
		fieldData.put("childOptionId1", childOptionId1);
		fieldData.put("childOptionId2", childOptionId2);

		return new Object[][] { { fieldData } };
	}
}
