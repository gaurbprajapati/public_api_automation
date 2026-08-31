package io.recruitcrm.albatross;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.albatross.EntityTypeCustomField;
import java.util.*;
import org.testng.Assert;
import java.lang.reflect.Method;
import io.rcrm.api.pojo.albatross.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetEntityCustomFields_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
    private String accountA_APIKey;
	String basePath = "entity-custom-fields/get";
    commanFunction function = new commanFunction();
    AllCrudFunctions privateFunction;

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        privateFunction = new AllCrudFunctions();
        accountA_APIKey = getAccountApiKey("AccountA");
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "createCustomFields", groups = "nightly-build")
    public void getEntityCustomFieldsCompany_Test(Integer compColId, Integer recordId, String entitySlug) {
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, false, requestBody);
		validateSuccessResponse(response, entitySlug);
    }	

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsUnauthorizedUser_Test(Integer compColId, Integer recordId, String entitySlug) {
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, false, requestBody);
		response.then().statusCode(401);
		response.then().body("error", Matchers.equalToIgnoringCase("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsEmptyToken_Test(Integer compColId, Integer recordId, String entitySlug) {
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, "", null, false, requestBody);
		response.then().statusCode(401);
		response.then().body("error", Matchers.equalToIgnoringCase("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsInvalidColId_Test(Integer compColId, Integer recordId, String entitySlug) {
		EntityTypeCustomField requestBody = createRequestBody(123, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("status", Matchers.equalToIgnoringCase("fail"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsCompanyCrossAccount_Test(Integer compColId, Integer recordId, String entitySlug) {
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalToIgnoringCase("is-success"));
		response.then().body("data.company.size()", Matchers.equalTo(0));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsCompanyAdminToken_Test(Integer compColId, Integer recordId, String entitySlug) {
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, adminToken, null, false, requestBody);
		validateSuccessResponse(response, entitySlug);
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsCompanyTeamMemberToken_Test(Integer compColId, Integer recordId, String entitySlug) {
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, teamMemberToken, null, false, requestBody);
		validateSuccessResponse(response, entitySlug);
	}

	// RBAC Bug - Created ERB for this - TITAN-21397
	@Owner("Smit Patel")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getEntityCustomFieldsCompanyRestrictedTeamMemberToken_Test(Integer compColId, Integer recordId, String entitySlug) {
		String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
		EntityTypeCustomField requestBody = createRequestBody(compColId, recordId);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, false, requestBody);
		validateSuccessResponse(response, entitySlug);
	}

	private EntityTypeCustomField createRequestBody(Integer compColId, Integer recordId) {
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setCompanyCustomFieldIds(Collections.singletonList("custcolumn" + compColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		requestBody.setCandidateCustomFieldIds(Collections.emptyList());
        requestBody.setContactCustomFieldIds(Collections.emptyList());
        requestBody.setDealCustomFieldIds(Collections.emptyList());
        requestBody.setJobCustomFieldIds(Collections.emptyList());

		return requestBody;
	}

	private void validateSuccessResponse(Response response, String entitySlug) {
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalToIgnoringCase("is-success"));
		response.then().body("message", Matchers.equalToIgnoringCase("Fetched custom field records"));
		response.then().body("data.company['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@DataProvider
	public Object[][] createCustomFields(Method method) {
		String companySlug = createEntity();
		int companyID = getEntityId(companySlug);
		int entityId = createCustomField("company", "companyField", "Company");
		updateCustomField("company", companyID, albatrossTknA, "custcolumn" + entityId, companySlug);
		return new Object[][] { { entityId, companyID, companySlug } };
	}

	public void updateCustomField(String entityType, int entityId, String albatrossAuthToken, String key, String value) {
		List<Integer> entityIds = Arrays.asList(entityId);
		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(key);
		updateFields.setValue(value);
		updateFields.setTableFlag(entityType);
		updateFields.setId(entityIds);
		Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
		Assert.assertEquals(response.getStatusCode(), 200);
	}

	public int createCustomField(String entityName, String customFieldName, String customFieldType) {
		Response resp = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, entityName, customFieldName,customFieldType, "");
		int id = resp.jsonPath().get("data.custumField.columnid");
		return id;
	}

	public String createEntity() {
		JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, accountA_APIKey).jsonPath();
		return json.get("slug");
	}

	public int getEntityId(String entitySlug) {
		return privateFunction.getCompanyResponse(albatrossURL, albatrossTknA, entitySlug).jsonPath().get("data.company.id");
	}
}
