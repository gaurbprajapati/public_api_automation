package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import java.util.*;
import org.json.JSONObject;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.CreateInvoiceRequest;
import io.rcrm.api.pojo.CustomField;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.invoiceService.InvoiceTemplate;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class CreateInvoiceAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	String albatrossTknB;
	int accountOwnerId;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerInvoice fakerInvoice;
	JavaFakerCustomField customFieldFaker;
	JavaFakerCandidate fakerCandidate;
	int internalCustomFieldColumnId = 1;

	String basePath = "invoices";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		fakerInvoice = new JavaFakerInvoice();
		customFieldFaker = new JavaFakerCustomField();
		fakerCandidate = new JavaFakerCandidate();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getStandardInvoiceTemplateData", groups = "nightly-build")
	public void createInvoiceWithStandardTemplate_PublicAPI(int templateId, Object invoiceFields, String companySlug, String contactSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInvoice_fields(invoiceFields != null ? invoiceFields : new ArrayList<>());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceTemplateWithStandardEntityFieldsData", groups = "nightly-build")
	public void createInvoiceWithStandardEntityFieldsData_PublicAPI(int templateId, Object invoiceFields, String companySlug, String contactSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setContact_slug(contactSlug);
		body.setInvoice_fields(invoiceFields != null ? invoiceFields : new ArrayList<>());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceTemplateWithCustomFieldsData", groups = "nightly-build")
	public void createInvoiceWithCustomFields_PublicAPI(int templateId, Object invoiceFields, String companySlug, String contactSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setContact_slug(contactSlug);
		body.setInvoice_fields(invoiceFields != null ? invoiceFields : new ArrayList<>());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceTemplateWithEntityTypeCustomFieldsData", groups = "nightly-build")
	public void createInvoiceWithEntityTypeCustomFields_PublicAPI(int templateId, Object invoiceFields, String companySlug, String contactSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setContact_slug(contactSlug);
		body.setInvoice_fields(invoiceFields != null ? invoiceFields : new ArrayList<>());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceWithInternalDateTypeCustomFieldData")
	public void createInvoiceWithInternalStandardTypeCustomFields_PublicAPI(int templateId, String companySlug, int customFieldId, String customFieldName, String customFieldValue) {

		List<CustomField> internalCustomFields = Arrays.asList(new CustomField(customFieldId, customFieldValue, null, null, null));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInternal_custom_fields(internalCustomFields);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));

		int invoiceId = response.jsonPath().getInt("invoice_id");

		Response invoiceResponse = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId, apiKeyA, null, null, true);

		invoiceResponse.then().statusCode(200);
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_name", Matchers.is(customFieldName));
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_type", Matchers.is("date"));
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].value", Matchers.startsWith(customFieldValue));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "createInvoiceWithInternalCandidateCustomFieldData")
	public void createInvoiceWithInternalEntityTypeCustomFields_PublicAPI(int templateId, String companySlug, int customFieldId, String customFieldName, String candidateSlug) {

		List<CustomField> internalCustomFields = Arrays.asList(new CustomField(customFieldId, candidateSlug, null, null, null));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInternal_custom_fields(internalCustomFields);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));

		int invoiceId = response.jsonPath().getInt("invoice_id");

		Response invoiceResponse = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId, apiKeyA, null, null, true);

		invoiceResponse.then().statusCode(200);
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_name", Matchers.is(customFieldName));
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_type", Matchers.is("candidate"));
		invoiceResponse.then().assertThat().body("internal_custom_fields[0].value", Matchers.is(candidateSlug));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData")
	public void createInvoiceWithInvalidInternalCustomFields_PublicAPI(int templateId, String companySlug) {

		int invalidFieldId = fakerInvoice.getRandomInvoiceId();
		List<CustomField> internalCustomFields = Arrays.asList(new CustomField(invalidFieldId, customFieldFaker.getRandomCustomTextValue(), null, null, null));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInternal_custom_fields(internalCustomFields);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("internal_custom_fields[0]", Matchers.is("internal custom fields are not valid."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getStandardInvoiceTemplateData", groups = "nightly-build")
	public void createInvoiceWithAllStandardFields_PublicAPI(int templateId, Object invoiceFields, String companySlug, String contactSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setContact_slug(contactSlug);
		body.setInvoice_fields(invoiceFields != null ? invoiceFields : new ArrayList<>());
		body.setAddress(fakerInvoice.getAddress());
		body.setContact_number(fakerInvoice.getContactNumber());
		body.setEmail(fakerInvoice.getEmail());
		body.setIssue_date(fakerInvoice.getRandomInvoicePastDate());
		body.setDue_date(fakerInvoice.getRandomInvoiceFutureDate());
		body.setAdditional_note(fakerInvoice.getDescription());
		body.setCurrency_id(fakerInvoice.getCurrencyId());
		body.setAssociated_company_slugs(companySlug);
		body.setAssociated_contact_slugs(contactSlug);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getAssociationsAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithAllAssociationsFields_PublicAPI(int templateId, String companySlug, String contactSlug, String candidateSlug, String jobSlug, String dealSlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setAssociated_company_slugs(companySlug);
		body.setAssociated_contact_slugs(contactSlug);
		body.setAssociated_candidate_slugs(candidateSlug);
		body.setAssociated_job_slugs(jobSlug);
		body.setAssociated_deal_slugs(dealSlug);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice created successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createInvoiceWithEmptyData_PublicAPI() {

		CreateInvoiceRequest createInvoiceRequest = new CreateInvoiceRequest();

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, createInvoiceRequest);

		response.then().statusCode(422);
		response.then().assertThat().body("template_id[0]", Matchers.is("The template id field is required."));
		response.then().assertThat().body("company_slug[0]", Matchers.is("The company slug field is required."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));

	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createInvoiceWithRandomData_PublicAPI() {

		CreateInvoiceRequest createInvoiceRequest = new CreateInvoiceRequest();
		createInvoiceRequest.setTemplate_id(fakerInvoice.getRandomTemplateId());
		createInvoiceRequest.setCompany_slug(fakerInvoice.getRandomSlug());
		createInvoiceRequest.setCurrency_id(fakerInvoice.getRandomTemplateId());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, createInvoiceRequest);

		response.then().statusCode(422);
		response.then().assertThat().body("template_id[0]", Matchers.is("The selected template id is invalid."));
		response.then().assertThat().body("company_slug[0]", Matchers.is("The selected company slug is invalid."));
		response.then().assertThat().body("currency_id[0]", Matchers.is("The selected currency id is invalid."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));

	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithWithInvalidInvoiceFields_PublicAPI(int templateId, String companySlug) {

		int invalidFieldId = fakerInvoice.getRandomInvoiceId();
		List<Map<String, Object>> row = new ArrayList<>();
		Map<String, Object> field = new HashMap<>();
		field.put("fieldId", invalidFieldId);
		field.put("fieldValue", fakerInvoice.getRandomInvoiceId());
		row.add(field);
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInvoice_fields(invoiceFields);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is( "Invalid field id: " + invalidFieldId + " in invoice fields which is not present in invoice template"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithWithInvalidAmountType_PublicAPI(int templateId, String companySlug) {

		List<Map<String, Object>> row = new ArrayList<>();
		Map<String, Object> field = new HashMap<>();
		field.put("fieldId", 1);
		field.put("fieldValue", fakerInvoice.getRandomPrefix());
		row.add(field);
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);
		body.setInvoice_fields(invoiceFields);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Field ID 1 (Amount): The value must be a number with at most 2 decimal places."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithInvalidDueDate_PublicAPI(int templateId, String companySlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setCompany_slug(companySlug);
		body.setIssue_date(fakerInvoice.getRandomInvoiceFutureDate());
		body.setDue_date(fakerInvoice.getRandomInvoicePastDate());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Due date cannot be before issue date"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithDescriptionMoreThan500Characters_PublicAPI(int templateId, String companySlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setCompany_slug(companySlug);
		body.setDescription(fakerInvoice.getRandomTextWithMoreThan500Chars());
		body.setAdditional_note(fakerInvoice.getRandomTextWithMoreThan500Chars());

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA, null, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("description[0]",
				Matchers.is("The description may not be greater than 500 characters."));
		response.then().assertThat().body("additional_note[0]", Matchers.is("The additional note may not be greater than 500 characters."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createInvoiceWithInvalidToken_PublicAPI() {

		CreateInvoiceRequest body = new CreateInvoiceRequest();

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyA + "123", null, true, body);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithCrossAccountTemplate_PublicAPI(int templateId, String companySlug) {

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyB, null, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice template not found"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCompanyAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithCrossAccountCompany_PublicAPI(int templateId, String companySlug) {

		Response templateResponse = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyB, null, null, true);
		templateResponse.then().statusCode(200);
		int currentAccountTemplateId = templateResponse.jsonPath().getInt("[0].template_id");

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(currentAccountTemplateId);
		body.setDescription(fakerInvoice.getDescription());
		body.setCompany_slug(companySlug);

		Response response = RestClient.doPost("JSON", baseURL, basePath, apiKeyB, null, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Company not found"));
	}

	@DataProvider
	public Object[][] getCompanyAndTemplateData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");

		Response response = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyA, null, null, true);
		response.then().statusCode(200);
		int templateId = response.jsonPath().getInt("[0].template_id");

		return new Object[][] { { templateId, companySlug } };
	}

	@DataProvider
	public Object[][] createInvoiceTemplateWithCustomFieldsData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
		String companySlug = companyResponse.jsonPath().getString("slug");
		Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
		String contactSlug = contactResponse.jsonPath().getString("slug");

		Response response = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
		response.then().statusCode(200);
		String sfdtContent = response.jsonPath().get("sfdt");

		Response textCustomFieldResponse = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, "Text Custom Field", "text", "");
		int textCustomFieldId = textCustomFieldResponse.jsonPath().getInt("data.fieldId");

		Response dateCustomFieldResponse = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, "Date Custom Field", "date", "");
		int dateCustomFieldId = dateCustomFieldResponse.jsonPath().getInt("data.fieldId");

		Object[][] itemConfigs = { 
				{ textCustomFieldId, 1, "Text Custom Field", 1 },
				{ dateCustomFieldId, 3, "Date Custom Field", 2 }, 
				{ 1, 4, "Amount", 3 } };
		List<JSONObject> templateItems = new ArrayList<>();
		for (Object[] config : itemConfigs) {
			JSONObject item = new JSONObject();
			item.put("formula", JSONObject.NULL);
			item.put("field_id", config[0]);
			item.put("field_type", config[1]);
			item.put("field_label", config[2]);
			item.put("default_field_label", config[2]);
			item.put("sequence_number", config[3]);
			templateItems.add(item);
		}

		InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(fakerInvoice.getInvoiceTemplateName(), Arrays.asList(accountOwnerId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);

		Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplate);
		int templateId = createResponse.jsonPath().getInt("data.id");

		Object[][] fieldConfigs = { 
				{ textCustomFieldId, fakerInvoice.getRandomInvoiceText() },
				{ dateCustomFieldId, fakerInvoice.getRandomInvoiceDate() }, 
				{ 1, fakerInvoice.getRandomInvoiceId() } };
		List<Map<String, Object>> row = new ArrayList<>();
		for (Object[] fc : fieldConfigs) {
			Map<String, Object> field = new HashMap<>();
			field.put("fieldId", fc[0]);
			field.put("fieldValue", fc[1]);
			row.add(field);
		}
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		return new Object[][] { { templateId, invoiceFields, companySlug, contactSlug } };
	}

	@DataProvider
	public Object[][] createInvoiceTemplateWithEntityTypeCustomFieldsData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
		String companySlug = companyResponse.jsonPath().getString("slug");
		Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
		String contactSlug = contactResponse.jsonPath().getString("slug");

		List<String> userIds = Arrays.asList(String.valueOf(accountOwnerId));
		Response createTeamResponse = allCrudFunctions.createTeam(albatrossURL, albatrossTknA, "invoiceTeam", userIds);
		createTeamResponse.then().statusCode(200);

		Response teamsResponse = function.getTeams(baseURL, apiKeyA);
		teamsResponse.then().statusCode(200);
		int teamId = teamsResponse.jsonPath().getInt("[0].team_id");

		Response response = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
		response.then().statusCode(200);
		String sfdtContent = response.jsonPath().get("sfdt");

		Response userCustomFieldResponse = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, "User Custom Field", "user", "");
		int userCustomFieldId = userCustomFieldResponse.jsonPath().getInt("data.fieldId");

		Response teamCustomFieldResponse = function.createInvoiceCustomFieldsResponse(invoiceServiceURL, albatrossTknA, "Team Custom Field", "team", "");
		int teamCustomFieldId = teamCustomFieldResponse.jsonPath().getInt("data.fieldId");

		Object[][] itemConfigs = { 
				{ userCustomFieldId, 12, "User Custom Field", 1 },
				{ teamCustomFieldId, 13, "Team Custom Field", 2 }, 
				{ 1, 4, "Amount", 3 } };
		List<JSONObject> templateItems = new ArrayList<>();
		for (Object[] config : itemConfigs) {
			JSONObject item = new JSONObject();
			item.put("formula", JSONObject.NULL);
			item.put("field_id", config[0]);
			item.put("field_type", config[1]);
			item.put("field_label", config[2]);
			item.put("default_field_label", config[2]);
			item.put("sequence_number", config[3]);
			templateItems.add(item);
		}

		InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(fakerInvoice.getInvoiceTemplateName(), Arrays.asList(accountOwnerId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);

		Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplate);
		createResponse.then().statusCode(201);
		int templateId = createResponse.jsonPath().getInt("data.id");

		Object[][] fieldConfigs = { 
				{ userCustomFieldId, accountOwnerId }, 
				{ teamCustomFieldId, teamId },
				{ 1, fakerInvoice.getRandomInvoiceId() } };
		List<Map<String, Object>> row = new ArrayList<>();
		for (Object[] fc : fieldConfigs) {
			Map<String, Object> field = new HashMap<>();
			field.put("fieldId", fc[0]);
			field.put("fieldValue", fc[1]);
			row.add(field);
		}
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		return new Object[][] { { templateId, invoiceFields, companySlug, contactSlug } };
	}

	@DataProvider
	public Object[][] createInvoiceTemplateWithStandardEntityFieldsData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		Response companyResponse = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA);
		String companySlug = companyResponse.jsonPath().getString("slug");
		Response contactResponse = function.createNewContact_POST(baseURL, apiKeyA, companySlug);
		String contactSlug = contactResponse.jsonPath().getString("slug");

		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String jobSlug = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug).jsonPath().getString("slug");
		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, apiKeyA).jsonPath().get("slug");

		Response customFieldsResponse = RestClient.doGet("JSON", invoiceServiceURL, "invoices/custom-fields", albatrossTknA, null, null, false);
		customFieldsResponse.then().statusCode(200);

		int candidateFieldId = customFieldsResponse.jsonPath().getInt("data[8].fieldId");
		int companyFieldId = customFieldsResponse.jsonPath().getInt("data[9].fieldId");
		int contactFieldId = customFieldsResponse.jsonPath().getInt("data[10].fieldId");
		int jobFieldId = customFieldsResponse.jsonPath().getInt("data[11].fieldId");
		int dealFieldId = customFieldsResponse.jsonPath().getInt("data[12].fieldId");
		int amountFieldId = customFieldsResponse.jsonPath().getInt("data[0].fieldId");

		Object[][] itemConfigs = { 
				{ candidateFieldId, 7, "Candidate Name", 1 },
				{ companyFieldId, 8, "Company Name", 2 }, 
				{ contactFieldId, 9, "Contact Name", 3 },
				{ jobFieldId, 10, "Job Name", 4 }, 
				{ dealFieldId, 11, "Deal Name", 5 },
				{ amountFieldId, 4, "Amount", 6 } };
		List<JSONObject> templateItems = new ArrayList<>();
		for (Object[] config : itemConfigs) {
			JSONObject item = new JSONObject();
			item.put("formula", JSONObject.NULL);
			item.put("field_id", config[0]);
			item.put("field_type", config[1]);
			item.put("field_label", config[2]);
			item.put("default_field_label", config[2]);
			item.put("sequence_number", config[3]);
			templateItems.add(item);
		}

		Response response = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
		response.then().statusCode(200);
		String sfdtContent = response.jsonPath().get("sfdt");

		InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(fakerInvoice.getInvoiceTemplateName(), Arrays.asList(accountOwnerId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);

		Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplate);
		int templateId = createResponse.jsonPath().getInt("data.id");

		Object[][] fieldConfigs = { 
				{ candidateFieldId, candidateSlug }, 
				{ companyFieldId, companySlug },
				{ contactFieldId, contactSlug }, 
				{ jobFieldId, jobSlug }, 
				{ dealFieldId, dealSlug },
				{ amountFieldId, fakerInvoice.getRandomInvoiceId() } };
		List<Map<String, Object>> row = new ArrayList<>();
		for (Object[] fc : fieldConfigs) {
			Map<String, Object> field = new HashMap<>();
			field.put("fieldId", fc[0]);
			field.put("fieldValue", fc[1]);
			row.add(field);
		}
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		return new Object[][] { { templateId, invoiceFields, companySlug, contactSlug } };
	}

	@DataProvider
	public Object[][] getStandardInvoiceTemplateData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiKeyA, companySlug).jsonPath().getString("slug");

		Response response = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyA, null, null, true);
		response.then().statusCode(200);
		int templateId = response.jsonPath().getInt("[0].template_id");

		Object[][] fieldConfigs = { 
				{ 3, fakerInvoice.getCurrencyId() }, 
				{ 4, fakerInvoice.getInvoiceStatusId() },
				{ 8, fakerInvoice.getDescription() }, 
				{ 1, fakerInvoice.getRandomInvoiceId() } };
		List<Map<String, Object>> row = new ArrayList<>();
		for (Object[] fc : fieldConfigs) {
			Map<String, Object> field = new HashMap<>();
			field.put("fieldId", fc[0]);
			field.put("fieldValue", fc[1]);
			row.add(field);
		}
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		return new Object[][] { { templateId, invoiceFields, companySlug, contactSlug } };
	}

	@DataProvider
	public Object[][] getAssociationsAndTemplateData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiKeyA, companySlug).jsonPath().getString("slug");
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String jobSlug = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug).jsonPath().getString("slug");
		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, apiKeyA).jsonPath().get("slug");

		Response response = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyA, null, null, true);
		response.then().statusCode(200);
		int templateId = response.jsonPath().getInt("[0].template_id");

		return new Object[][] { { templateId, companySlug, contactSlug, candidateSlug, jobSlug, dealSlug } };
	}

	@DataProvider
	public Object[][] createInvoiceWithInternalDateTypeCustomFieldData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");

		Response templateResponse = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyA, null, null, true);
		templateResponse.then().statusCode(200);
		int templateId = templateResponse.jsonPath().getInt("[0].template_id");

		String customFieldName = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "invoice", customFieldName, "date", "", internalCustomFieldColumnId);
		int customFieldId = customFieldResponse.jsonPath().getInt("data.custumField.columnid");
		String customFieldValue = fakerInvoice.getRandomInvoiceDate();

		return new Object[][] { { templateId, companySlug, customFieldId, customFieldName, customFieldValue } };
	}

	@DataProvider
	public Object[][] createInvoiceWithInternalCandidateCustomFieldData() {
		function.addBusinessDetails(invoiceServiceURL, albatrossTknA);

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");

		Response templateResponse = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyA, null, null, true);
		templateResponse.then().statusCode(200);
		int templateId = templateResponse.jsonPath().getInt("[0].template_id");

		String customFieldName = customFieldFaker.getCustomFieldName("invoice");
		Response customFieldResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "invoice", customFieldName, "candidate", "", internalCustomFieldColumnId);
		int customFieldId = customFieldResponse.jsonPath().getInt("data.custumField.columnid");

		return new Object[][] { { templateId, companySlug, customFieldId, customFieldName, candidateSlug } };
	}
}