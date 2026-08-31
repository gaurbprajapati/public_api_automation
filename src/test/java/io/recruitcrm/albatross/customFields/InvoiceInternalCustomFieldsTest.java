package io.recruitcrm.albatross.customFields;

import java.util.*;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class InvoiceInternalCustomFieldsTest extends TestBase {

	private final JavaFakerCustomField faker = new JavaFakerCustomField();
	private commanFunction function;
	private String albatrossAuthToken;
	private String invalidAuthToken;
	int columnId = 1;
	int invoiceEntityTypeId = 16;

	public InvoiceInternalCustomFieldsTest() {
		super();
	}

	@BeforeClass
	public void setUp() {
		function = new commanFunction();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		invalidAuthToken = albatrossAuthToken + "123";
	}

	// --- custom-fields (create) ---
	@Owner("Sai Teja SG")
	@Test
	public void createInvoiceInternalCustomField_Test() {
		String customFieldName = faker.getCustomFieldName("invoices");
		String customFieldType = faker.getRandomCustomFieldType();

		CustomFieldAlbatross payload = buildCustomFieldPayload(customFieldName, customFieldType);	
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, payload);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.custumField.entitytypeid", Matchers.is(invoiceEntityTypeId));
		response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
		response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
		response.then().body("data.custumField.columnid", Matchers.is(columnId));
	}

	@Owner("Sai Teja SG")
	@Test
	public void createInvoiceInternalEntityTypeCustomField_Test() {
		String customFieldName = faker.getCustomFieldName("invoices");
		String customFieldType = faker.getRandomEntityTypeCustomFieldType();

		CustomFieldAlbatross payload = buildCustomFieldPayload(customFieldName, customFieldType);	
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, payload);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.custumField.entitytypeid", Matchers.is(invoiceEntityTypeId));
		response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
		response.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
		response.then().body("data.custumField.columnid", Matchers.is(columnId));
	}

	@Owner("Sai Teja SG")
	@Test
	public void createInvoiceInternalCustomFieldWithEmptyData_Test() {
		CustomFieldAlbatross empty = new CustomFieldAlbatross();
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, empty);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The custum field.extrafieldname field is required.,The custum field.entitytypeid field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}

	@Owner("Sai Teja SG")
	@Test
	public void createInvoiceInternalCustomFieldWithSameName_Test() {
		String customFieldName = faker.getCustomFieldName("invoices");
		String customFieldType = faker.getRandomCustomFieldType();

		CustomFieldAlbatross payload = buildCustomFieldPayload(customFieldName, customFieldType);	
		Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, payload);

		createResponse.then().statusCode(200);
		createResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, payload);

		response.then().statusCode(200);
		response.then().body("message", Matchers.is("Custom Field Already Exist"));
		response.then().body("message_type", Matchers.is("is-danger"));
		response.then().body("data", Matchers.empty());
	}
	
	@Owner("Sai Teja SG")
	@Test
	public void createInvoiceInternalCustomFieldWithUnauthorizedAccess_Test() {
		CustomFieldAlbatross payload = new CustomFieldAlbatross();
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", invalidAuthToken, null, true, payload);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	// --- custom-fields/get ---
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldData")
	public void getInvoiceInternalCustomFields_Test(int customFieldId, String customFieldName, String fieldType) {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entitytypeid", String.valueOf(invoiceEntityTypeId));

		Response response = RestClient.doPost1("JSON", albatrossURL, "custom-fields/get", albatrossAuthToken, queryParameters, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.is("is_success"));
		response.then().body("data.custumFields", Matchers.hasSize(1));
		response.then().body("data.custumFields[0].columnid", Matchers.is(columnId));
		response.then().body("data.custumFields[0].entitytypeid", Matchers.is(invoiceEntityTypeId));
		response.then().body("data.custumFields[0].extrafieldname", Matchers.is(customFieldName));
		response.then().body("data.custumFields[0].extrafieldtype", Matchers.is(fieldType));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldData")
	public void getInvoiceInternalEntityTypeCustomFields_Test(int customFieldId, String fieldName, String fieldType) {
		String customFieldName = faker.getCustomFieldName("invoices");
		String customFieldType = faker.getRandomEntityTypeCustomFieldType();
		CustomFieldAlbatross payload = buildCustomFieldPayload(customFieldName, customFieldType);
		Response createResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, true, payload);

		createResponse.then().statusCode(200);
		createResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));

		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entitytypeid", String.valueOf(invoiceEntityTypeId));

		Response response = RestClient.doPost1("JSON", albatrossURL, "custom-fields/get", albatrossAuthToken, queryParameters, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.is("is_success"));
		response.then().body("data.custumFields", Matchers.hasSize(2));
		response.then().body("data.custumFields[0].columnid", Matchers.is(columnId));
		response.then().body("data.custumFields[0].entitytypeid", Matchers.is(invoiceEntityTypeId));
		response.then().body("data.custumFields[0].extrafieldname", Matchers.is(fieldName));
		response.then().body("data.custumFields[0].extrafieldtype", Matchers.is(fieldType));
	}

	@Owner("Sai Teja SG")
	@Test
	public void getInvoiceInternalCustomFieldsWithEmptyData_Test() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entitytypeid", String.valueOf(invoiceEntityTypeId));

		Response response = RestClient.doPost1("JSON", albatrossURL, "custom-fields/get", albatrossAuthToken, queryParameters, null, true, null);

		response.then().statusCode(200);
		response.then().body("message_type", Matchers.is("is_success"));
		response.then().body("data.custumFields", Matchers.empty());
	}

	@Owner("Sai Teja SG")
	@Test
	public void getInvoiceInternalCustomFieldsWithUnauthorizedAccess_Test() {
		Map<String, String> queryParameters = new HashMap<>();
		queryParameters.put("entitytypeid", String.valueOf(invoiceEntityTypeId));

		Response response = RestClient.doPost1("JSON", albatrossURL, "custom-fields/get", invalidAuthToken, queryParameters, null, true, null);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	// --- custom-fields/{id} (edit) ---
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldData")
	public void editInvoiceInternalCustomField_Test(int customFieldId, String originalFieldName, String customFieldType) {
		String updatedName = faker.getCustomFieldName("invoices");
		ExtraField updatedExtra = new ExtraField();
		updatedExtra.setColumnid(columnId);
		updatedExtra.setEntitytypeid(invoiceEntityTypeId);
		updatedExtra.setExtrafieldname(updatedName);
		updatedExtra.setExtrafieldtype(customFieldType);
		updatedExtra.setDefaultvalue("");
		updatedExtra.setDefaultoptionsvalue(new ArrayList<>());

		CustomFieldAlbatross updated = new CustomFieldAlbatross();
		updated.setCustumField(updatedExtra);

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(customFieldId));

		Response editResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/{id}", albatrossAuthToken, null, pathParams, true, updated);

		Assert.assertEquals(editResponse.getStatusCode(), 200);
		editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		editResponse.then().body("message_type", Matchers.is("is-success"));
		editResponse.then().body("data.custumField.entitytypeid", Matchers.is(invoiceEntityTypeId));
		editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedName));
		editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
		editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
		editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldData")
	public void editInvoiceInternalCustomField_Unauthorized_Test(int customFieldId, String originalFieldName, String customFieldType) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(customFieldId));
		
		Response editResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/{id}", invalidAuthToken, null, pathParams, true, null);

		Assert.assertEquals(editResponse.getStatusCode(), 401);
		editResponse.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void editInvoiceInternalCustomField_EmptyData_Test() {
		
		CustomFieldAlbatross empty = new CustomFieldAlbatross();
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(faker.getRandomEntityId()));

		Response editResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/{id}", albatrossAuthToken, null, pathParams, true, empty);

		Assert.assertEquals(editResponse.getStatusCode(), 422);
		editResponse.then().body("message", Matchers.is("The custum field.extrafieldname field is required.,The custum field.entitytypeid field is required."));
		editResponse.then().body("message_type", Matchers.is("is-danger"));
		editResponse.then().body("data", Matchers.empty());
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "invoiceEntityReferenceCustomFieldData")
	public void editInvoiceInternalEntityTypeCustomField_Test(int customFieldId, String originalFieldName, String customFieldType, int entityTypeId) {
		String updatedFieldName = faker.getRandomCustomFieldName();

		ExtraField updatedExtraField = new ExtraField();
		CustomFieldAlbatross updatedCustomField = new CustomFieldAlbatross();
		updatedExtraField.setColumnid(columnId);
		updatedExtraField.setEntitytypeid(entityTypeId);
		updatedExtraField.setExtrafieldname(updatedFieldName);
		updatedExtraField.setExtrafieldtype(customFieldType);
		updatedExtraField.setDefaultvalue("");
		updatedExtraField.setDefaultoptionsvalue(new ArrayList<>());
		updatedCustomField.setCustumField(updatedExtraField);

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(customFieldId));

		Response editResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/{id}", albatrossAuthToken, null, pathParams, true, updatedCustomField);
		Assert.assertEquals(editResponse.getStatusCode(), 200);
		editResponse.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		editResponse.then().body("message_type", Matchers.is("is-success"));
		editResponse.then().body("data.custumField.entitytypeid", Matchers.is(entityTypeId));
		editResponse.then().body("data.custumField.extrafieldname", Matchers.is(updatedFieldName));
		editResponse.then().body("data.custumField.extrafieldtype", Matchers.is(customFieldType));
		editResponse.then().body("data.custumField.columnid", Matchers.is(columnId));
		editResponse.then().body("data.custumField.id", Matchers.is(customFieldId));
	}

	// --- custom-fields/delete/{id} ---
	@Owner("Sai Teja SG")
	@Test(dataProvider = "getInvoiceInternalCustomFieldData")
	public void deleteInvoiceInternalCustomField_Test(int customFieldId, String originalFieldName, String customFieldType) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(customFieldId));

		Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/delete/{id}", albatrossAuthToken, null, pathParams, true, null);

		Assert.assertEquals(deleteResponse.getStatusCode(), 200);
		deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
		deleteResponse.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "invoiceEntityReferenceCustomFieldData")
	public void deleteInvoiceInternalEntityTypeCustomField_Test(int customFieldId, String originalFieldName, String customFieldType, int entityTypeId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(customFieldId));

		Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/delete/{id}", albatrossAuthToken, null, pathParams, true, null);

		Assert.assertEquals(deleteResponse.getStatusCode(), 200);
		deleteResponse.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
		deleteResponse.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void deleteInvoiceInternalCustomField_EmptyData_Test() {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(0));

		Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/delete/{id}", albatrossAuthToken, null, pathParams, true, null);

		Assert.assertEquals(deleteResponse.getStatusCode(), 200);
		deleteResponse.then().body("message", Matchers.is("Failed To Custom Field Deleted : "));
		deleteResponse.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Sai Teja SG")
	@Test
	public void deleteInvoiceInternalCustomField_Unauthorized_Test() {
		
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("id", String.valueOf(faker.getRandomEntityId()));

		Response deleteResponse = RestClient.doPost1("JSON", albatrossURL, "custom-fields/delete/{id}", invalidAuthToken, null, pathParams, true, null);

		Assert.assertEquals(deleteResponse.getStatusCode(), 401);
		deleteResponse.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider
	public Object[][] invoiceEntityReferenceCustomFieldData() {
		String customFieldType = faker.getRandomEntityTypeCustomFieldType();
		String customFieldName = faker.getRandomCustomFieldName();

		Response createResponse = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "invoice", customFieldName, customFieldType, "", columnId);
		int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

		return new Object[][] {
				{ customFieldId, customFieldName, customFieldType, invoiceEntityTypeId } };
	}

	@DataProvider
	public Object[][] getInvoiceInternalCustomFieldData() {
		String customFieldName = faker.getCustomFieldName("invoices");
		String customFieldType = faker.getRandomCustomFieldType();
		String defaultOptions = faker.getNumberOfDefaultOptionsValues(3);

		Response createResponse = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, "invoice", customFieldName, customFieldType, defaultOptions, columnId);
		int customFieldId = createResponse.jsonPath().getInt("data.custumField.id");

		return new Object[][] { { customFieldId, customFieldName, customFieldType } };
	}


	private CustomFieldAlbatross buildCustomFieldPayload(String customFieldName, String customFieldType) {
		ExtraField extraField = new ExtraField();
		extraField.setColumnid(columnId);
		extraField.setEntitytypeid(invoiceEntityTypeId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(customFieldType);

		if (customFieldType.equalsIgnoreCase("dropdown") || customFieldType.equalsIgnoreCase("multiselect")) {
			String customFieldOptions = faker.getNumberOfDefaultOptionsValues(3);
			List<DefaultOptionsValue> optionsList = new ArrayList<>();
			String[] options = customFieldOptions.split(",");
			for (int i = 0; i < options.length; i++) {
				DefaultOptionsValue option = new DefaultOptionsValue();
				option.setLabel(options[i].trim());
				option.setSequence_no(i + 1);
				option.setTempId(faker.getTempId());
				optionsList.add(option);
			}
			extraField.setDefaultoptionsvalue(optionsList);
			extraField.setDefaultvalue(null);
		} else {
			extraField.setDefaultvalue(null);
			extraField.setDefaultoptionsvalue(new ArrayList<>());
		}

		CustomFieldAlbatross customField = new CustomFieldAlbatross();
		customField.setCustumField(extraField);
		return customField;
	}

}