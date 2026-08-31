package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.annotations.*;

import java.util.*;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.CreateInvoiceRequest;
import io.rcrm.api.pojo.invoiceService.InvoiceTemplate;
import io.rcrm.api.pojo.CustomField;
import io.rcrm.api.javafaker.JavaFakerInvoice;
import org.json.JSONObject;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class EditInvoiceByIdAPI_Test extends TestBase {

	String apiKeyA;
	String apiKeyB;
	String albatrossTknA;
	int accountOwnerId;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	JavaFakerInvoice fakerInvoice;
	JavaFakerCustomField customFieldFaker;
	int internalCustomFieldColumnId = 1;
	String basePath = "invoices/{id}";

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		apiKeyB = getAccountApiKey("AccountB");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		accountOwnerId = getRoleBasedId("AccountA", "Owner");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
		fakerInvoice = new JavaFakerInvoice();
		customFieldFaker = new JavaFakerCustomField();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithValidToken_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setDescription(fakerInvoice.getDescription());
		body.setAddress(fakerInvoice.getAddress());
		body.setContact_number(fakerInvoice.getContactNumber());
		body.setEmail(fakerInvoice.getEmail());
		body.setIssue_date(fakerInvoice.getRandomInvoicePastDate());
		body.setDue_date(fakerInvoice.getRandomInvoiceFutureDate());
		body.setAdditional_note(fakerInvoice.getDescription());
		body.setCurrency_id(fakerInvoice.getCurrencyId());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.equalTo(invoiceId));
		response.then().assertThat().body("message", Matchers.is("Invoice Updated successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//updateInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndCandidateData", groups = "nightly-build")
	public void editInvoiceWithStandardEntityFieldsData_PublicAPI(int invoiceId, int templateId, int candidateFieldId, String candidateSlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		List<Map<String, Object>> row = new ArrayList<>();
		Map<String, Object> field = new HashMap<>();
		field.put("fieldId", candidateFieldId);
		field.put("fieldValue", candidateSlug);
		row.add(field);
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(templateId);
		body.setInvoice_fields(invoiceFields);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice Updated successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//updateInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndAssociationsData", groups = "nightly-build")
	public void editInvoiceWithAssociationsFieldsData_PublicAPI(int invoiceId, String companySlug, String contactSlug, String candidateSlug, String jobSlug, String dealSlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setAssociated_company_slugs(companySlug);
		body.setAssociated_contact_slugs(contactSlug);
		body.setAssociated_candidate_slugs(candidateSlug);
		body.setAssociated_job_slugs(jobSlug);
		body.setAssociated_deal_slugs(dealSlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(200);
		response.then().assertThat().body("invoice_id", Matchers.notNullValue());
		response.then().assertThat().body("message", Matchers.is("Invoice Updated successfully"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//updateInvoiceAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithoutChanges_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setCompany_slug(companySlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("errorMessage", Matchers.is("At least one value must change!"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editInvoiceByIdWithInvalidId_PublicAPI() {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		CreateInvoiceRequest body = new CreateInvoiceRequest();

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//invoicesNotExistsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithInvalidInvoiceFields_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		int invalidFieldId = fakerInvoice.getRandomInvoiceId();
		List<Map<String, Object>> row = new ArrayList<>();
		Map<String, Object> field = new HashMap<>();
		field.put("fieldId", invalidFieldId);
		field.put("fieldValue", fakerInvoice.getRandomInvoiceId());
		row.add(field);
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setInvoice_fields(invoiceFields);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is( "Invalid field id: " + invalidFieldId + " in invoice fields which is not present in invoice template"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithInvalidAmountValue_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		List<Map<String, Object>> row = new ArrayList<>();
		Map<String, Object> field = new HashMap<>();
		field.put("fieldId", 1);
		field.put("fieldValue", fakerInvoice.getRandomPrefix());
		row.add(field);
		List<List<Map<String, Object>>> invoiceFields = new ArrayList<>();
		invoiceFields.add(row);

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setInvoice_fields(invoiceFields);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Field ID 1 (Amount): The value must be a number with at most 2 decimal places."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithInvalidDueDate_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setIssue_date(fakerInvoice.getRandomInvoiceFutureDate());
		body.setDue_date(fakerInvoice.getRandomInvoicePastDate());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Due date cannot be before issue date"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithDescriptionMoreThan500Characters_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setDescription(fakerInvoice.getRandomTextWithMoreThan500Chars());
		body.setAdditional_note(fakerInvoice.getRandomTextWithMoreThan500Chars());

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("description[0]",
				Matchers.is("The description may not be greater than 500 characters."));
		response.then().assertThat().body("additional_note[0]", Matchers.is("The additional note may not be greater than 500 characters."));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void editInvoiceByIdWithEmptyData_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(422);
		response.then().assertThat().body("errorMessage", Matchers.is("At least one value must change!"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editInvoiceByIdWithInvalidToken_PublicAPI() {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(fakerInvoice.getRandomInvoiceId()));

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA + "123", null, pathParams, true, null);

		response.then().statusCode(401);
		response.then().assertThat().body("error", Matchers.is("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithCrossAccountInvoice_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		CreateInvoiceRequest body = new CreateInvoiceRequest();

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyB, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithCrossAccountCompany_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		String crossAccountCompanySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyB).jsonPath().getString("slug");

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setCompany_slug(crossAccountCompanySlug);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Company not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData", groups = "nightly-build")
	public void createInvoiceWithCrossAccountTemplate_PublicAPI(int invoiceId, String companySlug) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(invoiceId));

		Response templateResponse = RestClient.doGet("JSON", baseURL, "invoice-templates", apiKeyB, null, null, true);
		templateResponse.then().statusCode(200);
		int crossAccountTemplateId = templateResponse.jsonPath().getInt("[0].template_id");

		CreateInvoiceRequest body = new CreateInvoiceRequest();
		body.setTemplate_id(crossAccountTemplateId);

		Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

		response.then().statusCode(400);
		response.then().assertThat().body("errorMessage", Matchers.is("Invoice template not found"));

		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndInternalCustomFieldData")
    public void editInvoiceByIdWithInternalCustomFields_PublicAPI(int invoiceId, int customFieldId, String customFieldName, String customFieldValue) {

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", String.valueOf(invoiceId));

        List<CustomField> internalCustomFields = Arrays.asList(new CustomField(customFieldId, customFieldValue, null, null, null));
        CreateInvoiceRequest body = new CreateInvoiceRequest();
        body.setInternal_custom_fields(internalCustomFields);

        Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

        response.then().statusCode(200);
        response.then().assertThat().body("invoice_id", Matchers.equalTo(invoiceId));
        response.then().assertThat().body("message", Matchers.is("Invoice Updated successfully"));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//updateInvoiceAPI.json"));

        Response invoiceResponse = RestClient.doGet("JSON", baseURL, "invoices/" + invoiceId, apiKeyA, null, null, true);

        invoiceResponse.then().statusCode(200);
        invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_name", Matchers.is(customFieldName));
        invoiceResponse.then().assertThat().body("internal_custom_fields[0].field_type", Matchers.is("text"));
        invoiceResponse.then().assertThat().body("internal_custom_fields[0].value", Matchers.is(customFieldValue));
    }

    @Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceIdAndTemplateData")
    public void editInvoiceByIdWithInvalidInternalCustomFields_PublicAPI(int invoiceId, String companySlug) {

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("id", String.valueOf(invoiceId));

        int invalidFieldId = fakerInvoice.getRandomInvoiceId();
        List<CustomField> internalCustomFields = Arrays.asList(new CustomField(invalidFieldId, customFieldFaker.getRandomCustomTextValue(), null, null, null));
        CreateInvoiceRequest body = new CreateInvoiceRequest();
        body.setInternal_custom_fields(internalCustomFields);

        Response response = RestClient.doPost1("JSON", baseURL, basePath, apiKeyA, null, pathParams, true, body);

        response.then().statusCode(422);
        response.then().assertThat().body("internal_custom_fields[0]", Matchers.is("internal custom fields are not valid."));

        response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//createInvoiceValidationErrorsAPI.json"));
    }

	@DataProvider(parallel = true)
	public Object[][] getInvoiceIdAndTemplateData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = response.jsonPath().get("data");
		return new Object[][] { { invoiceId, companySlug } };
	}

	@DataProvider(parallel = true)
	public Object[][] getInvoiceIdAndAssociationsData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String contactSlug = function.createNewContact_POST(baseURL, apiKeyA, companySlug).jsonPath().getString("slug");
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		String jobSlug = function.createNewJob(baseURL, apiKeyA, companySlug, contactSlug).jsonPath().getString("slug");
		String dealSlug = function.createNewDealWithMandatoryFields(baseURL, apiKeyA).jsonPath().get("slug");

		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = response.jsonPath().get("data");
		return new Object[][] { { invoiceId, companySlug, contactSlug, candidateSlug, jobSlug, dealSlug } };
	}

	@DataProvider
	public Object[][] getInvoiceIdAndCandidateData() {
		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
		Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
		int invoiceId = response.jsonPath().get("data");

		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");

		Response customFieldsResponse = RestClient.doGet("JSON", invoiceServiceURL, "invoices/custom-fields", albatrossTknA, null, null, false);
		customFieldsResponse.then().statusCode(200);

		int candidateFieldId = customFieldsResponse.jsonPath().getInt("data[8].fieldId");

		List<JSONObject> templateItems = new ArrayList<>();
		JSONObject item = new JSONObject();
		item.put("formula", JSONObject.NULL);
		item.put("field_id", candidateFieldId);
		item.put("field_type", 7);
		item.put("field_label", "Candidate Name");
		item.put("default_field_label", "Candidate Name");
		item.put("sequence_number", 1);
		templateItems.add(item);

		Response tableResponse = function.generateInvoiceTableWithDefaultValues(syncFunctionURL, albatrossTknA);
		tableResponse.then().statusCode(200);
		String sfdtContent = tableResponse.jsonPath().get("sfdt");

		InvoiceTemplate invoiceTemplate = function.createInvoiceTemplatePayload(fakerInvoice.getInvoiceTemplateName(), Arrays.asList(accountOwnerId), new ArrayList<>(), "7 Days", sfdtContent, templateItems);

		Response createResponse = RestClient.doPost("JSON", invoiceServiceURL, "invoices/templates", albatrossTknA, null, true, invoiceTemplate);
		int templateId = createResponse.jsonPath().getInt("data.id");

		return new Object[][] { { invoiceId, templateId, candidateFieldId, candidateSlug } };
	}

	@DataProvider(parallel = true)
    public Object[][] getInvoiceIdAndInternalCustomFieldData() {
        String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
        int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().get("data.company.id");
        Response response = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId);
        int invoiceId = response.jsonPath().get("data");

        String customFieldName = customFieldFaker.getCustomFieldName("invoice");
        Response customFieldResponse = function.createCustomFieldsResponse(albatrossURL, albatrossTknA, "invoice", customFieldName, "text", "", internalCustomFieldColumnId);
        int customFieldId = customFieldResponse.jsonPath().getInt("data.custumField.columnid");
        String customFieldValue = customFieldFaker.getRandomCustomTextValue();

        return new Object[][] { { invoiceId, customFieldId, customFieldName, customFieldValue } };
    }

}