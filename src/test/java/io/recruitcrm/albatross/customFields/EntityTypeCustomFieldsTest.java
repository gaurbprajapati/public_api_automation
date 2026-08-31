package io.recruitcrm.albatross.customFields;

import com.qa.api.util.reaper.ThreadManager;
import java.util.*;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import java.lang.reflect.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.pojo.albatross.Contact.RelatedCompaniesRequest;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EntityTypeCustomFieldsTest extends TestBase {
	AllCrudFunctions privateFunction = new AllCrudFunctions();
	String albatrossTkn;
	String apiAuthToken;
	commanFunction function = new commanFunction();

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createContactWithCompany", groups = "nightly-build")
	public void getRelatedCompanies_Test(int contactId) {

		List<Integer> ids = Collections.singletonList(contactId);
		RelatedCompaniesRequest requestBody = new RelatedCompaniesRequest();
		requestBody.setContactIds(ids);
		requestBody.setFromListPage(true);
		String basePath = "contacts/get-related-companies";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("message", Matchers.equalTo("Related Companies Fetched Successfully"));
		response.then().body("status", Matchers.equalTo("success"));

		String contactIdStr = String.valueOf(contactId);
		response.then().body("data.company", Matchers.hasKey(contactIdStr));
		response.then().body("data.company['" + contactIdStr + "'][0].contact_id", Matchers.equalTo(contactId));
		response.then().body("data.company['" + contactIdStr + "'][0].name", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getRelatedCompaniesInvalidId_Test() {

		List<Integer> ids = Collections.singletonList(12345);
		RelatedCompaniesRequest requestBody = new RelatedCompaniesRequest();
		requestBody.setContactIds(ids);
		requestBody.setFromListPage(true);
		String basePath = "contacts/get-related-companies";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.company", Matchers.empty());
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getRelatedCompaniesUnauthorizedAccess_Test() {

		List<Integer> ids = Collections.singletonList(12345);
		RelatedCompaniesRequest requestBody = new RelatedCompaniesRequest();
		requestBody.setContactIds(ids);
		requestBody.setFromListPage(true);
		String basePath = "contacts/get-related-companies";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, false, requestBody);
		response.then().statusCode(401);
		response.then().body("error", Matchers.equalToIgnoringCase("Unauthorized"));
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields")
	public void getRelatedCandidateEntityData_Test(Integer candColId, Integer recordId, String entitySlug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setCandidateCustomFieldIds(Collections.singletonList("custcolumn" + candColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.candidate['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getRelatedCompanyEntityData_Test(Integer compColId, Integer recordId, String entitySlug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setCompanyCustomFieldIds(Collections.singletonList("custcolumn" + compColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		requestBody.setCandidateCustomFieldIds(Collections.emptyList());
        requestBody.setContactCustomFieldIds(Collections.emptyList());
        requestBody.setDealCustomFieldIds(Collections.emptyList());
        requestBody.setJobCustomFieldIds(Collections.emptyList());

		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.company['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getRelatedContactEntityData_Test(Integer contColId, Integer recordId, String entitySlug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setContactCustomFieldIds(Collections.singletonList("custcolumn" + contColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		requestBody.setCandidateCustomFieldIds(Collections.emptyList());
        requestBody.setCompanyCustomFieldIds(Collections.emptyList());
        requestBody.setDealCustomFieldIds(Collections.emptyList());
        requestBody.setJobCustomFieldIds(Collections.emptyList());

		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.contact['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getRelatedDealEntityData_Test(Integer dealColId, Integer recordId, String entitySlug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setDealCustomFieldIds(Collections.singletonList("custcolumn" + dealColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		requestBody.setCandidateCustomFieldIds(Collections.emptyList());
        requestBody.setCompanyCustomFieldIds(Collections.emptyList());
        requestBody.setContactCustomFieldIds(Collections.emptyList());
        requestBody.setJobCustomFieldIds(Collections.emptyList());

		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.deals['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getRelatedJobEntityData_Test(Integer jobColId, Integer recordId, String entitySlug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setJobCustomFieldIds(Collections.singletonList("custcolumn" + jobColId));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
		requestBody.setCandidateCustomFieldIds(Collections.emptyList());
        requestBody.setCompanyCustomFieldIds(Collections.emptyList());
        requestBody.setContactCustomFieldIds(Collections.emptyList());
        requestBody.setDealCustomFieldIds(Collections.emptyList());
		
		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("data.job['" + entitySlug + "'].id", Matchers.notNullValue());
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "createCustomFields", groups = "nightly-build")
	public void getRelatedCandidateEntityDataWithInvalidColId_Test(Integer candColId, Integer recordId, String entityslug) {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		requestBody.setCandidateCustomFieldIds(Collections.singletonList("custcolumn123"));
		requestBody.setRecordIds(Collections.singletonList(recordId));
		requestBody.setEntityTypeId(3);
        requestBody.setCompanyCustomFieldIds(Collections.emptyList());
        requestBody.setContactCustomFieldIds(Collections.emptyList());
        requestBody.setDealCustomFieldIds(Collections.emptyList());
        requestBody.setJobCustomFieldIds(Collections.emptyList());

		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn, null, false, requestBody);
		response.then().statusCode(200);
		response.then().body("message_type", Matchers.equalToIgnoringCase("is-danger"));
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void getRelatedEntityDataWithUnauthorizedUser_Test() {
		
		EntityTypeCustomField requestBody = new EntityTypeCustomField();
		String basePath = "entity-custom-fields/get";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTkn + "123", null, false, requestBody);
		response.then().statusCode(401);
		response.then().body("error", Matchers.equalToIgnoringCase("Unauthorized"));
	}

	@DataProvider
	public Object[][] createCustomFields(Method method) {
		int entityId;
		String entitySlug;
		String companySlug = createEntity("company");
		int companyID = getEntityId("company", companySlug);
		if (method.getName().contains("Candidate")) {
			entitySlug = createEntity("candidate");
			entityId = createCustomField("company", "candidateField", "Candidate");
		} else if (method.getName().contains("Company")) {
			entitySlug = createEntity("company");
			entityId = createCustomField("company", "companyField", "Company");
		} else if (method.getName().contains("Contact")) {
			entitySlug = createEntity("contact");
			entityId = createCustomField("company", "contactField", "Contact");
		} else if (method.getName().contains("Deal")) {
			entitySlug = createEntity("deal");
			entityId = createCustomField("company", "dealField", "Deal");
		} else {
			entitySlug = createEntity("job");
			entityId = createCustomField("company", "jobField", "Job");
		}
		updateCustomField("company", companyID, albatrossTkn, "custcolumn" + entityId, entitySlug);
		return new Object[][] { { entityId, companyID, entitySlug } };
	}

	@DataProvider
	public Object[][] createContactWithCompany() {
		String contactSlug = createEntity("contact");
		int contactId = getEntityId("contact", contactSlug);
		return new Object[][] { { contactId } };
	}

	public void updateCustomField(String entityType, int entityId, String albatrossAuthToken, String key, String value) {
		List<Integer> entityIds = Arrays.asList(entityId);
		UpdateFields updateFields = new UpdateFields();
		updateFields.setKey(key);
		updateFields.setValue(value);
		updateFields.setTableFlag(entityType);
		updateFields.setId(entityIds);
		String basePath = "global/update-fields";
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, true, updateFields);
		Assert.assertEquals(response.getStatusCode(), 200);
	}

	public int createCustomField(String entityName, String customFieldName, String customFieldType) {
		Response resp = function.createCustomFieldsResponse(albatrossURL, albatrossTkn, entityName, customFieldName,customFieldType, "");
		int id = resp.jsonPath().get("data.custumField.columnid");
		return id;
	}

	public String createEntity(String realtedToType) {
		String entitySlug = null;
		if (realtedToType.equals("candidate")) {
			JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType.equals("company")) {
			JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType.equals("contact")) {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath json = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
			entitySlug = json.get("slug");
		}
		if (realtedToType.equals("deal")) {
			JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
			entitySlug = jsonDeal.get("slug");
		}
		if (realtedToType.equals("job")) {
			JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
			String companySlug = jsonCompany.get("slug");
			JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
			String contactSlug = jsonContact.get("slug");
			JsonPath json = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
			entitySlug = json.get("slug");
		}
		return entitySlug;
	}

	public int getEntityId(String entityType, String entitySlug) {
		int entityId = 0;
		if (entityType.equals("candidate")) {
			entityId = privateFunction.getCandidateResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.candidate.id");
		}
		if (entityType.equals("company")) {
			entityId = privateFunction.getCompanyResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.company.id");
		}
		if (entityType.equals("contact")) {
			entityId = Integer.parseInt(privateFunction.getContactResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.contact.id"));
		}
		if (entityType.equals("deal")) {
			entityId = privateFunction.getDealResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.contact.id");
		}
		if (entityType.equals("job")) {
			entityId = privateFunction.getJobResponse(albatrossURL, albatrossTkn, entitySlug).jsonPath().get("data.contact.id");
		}
		return entityId;
	}
}
