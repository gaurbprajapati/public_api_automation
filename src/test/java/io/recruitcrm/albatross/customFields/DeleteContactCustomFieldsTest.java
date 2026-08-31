package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.DefaultOptionsValue;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class DeleteContactCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String apiAuthToken;
	private static final int CONTACT_ENTITY_TYPE_ID = 2;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "textCustomFieldData", groups = "nightly-build")
	public void deleteContactTextCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("text", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "numberCustomFieldData", groups = "nightly-build")
	public void deleteContactNumberCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("number", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dateCustomFieldData", groups = "nightly-build")
	public void deleteContactDateCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("date", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "dateTimeCustomFieldData", groups = "nightly-build")
	public void deleteContactDateTimeCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("date_time", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "longTextCustomFieldData", groups = "nightly-build")
	public void deleteContactLongTextCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("longtext", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "phoneCustomFieldData", groups = "nightly-build")
	public void deleteContactPhoneCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("phonenumber", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "emailCustomFieldData", groups = "nightly-build")
	public void deleteContactEmailCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("email", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "dropdownCustomFieldData", groups = "nightly-build")
	public void deleteContactDropdownCustomField_Test(String fieldId, String fieldName, String columnId, List<Map<String, Object>> options) {
		CustomFieldAlbatross requestData = createDeleteRequestDataWithOptions("dropdown", fieldName, columnId, options);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "multiselectCustomFieldData", groups = "nightly-build")
	public void deleteContactMultiselectCustomField_Test(String fieldId, String fieldName, String columnId, List<Map<String, Object>> options) {
		CustomFieldAlbatross requestData = createDeleteRequestDataWithOptions("multiselect", fieldName, columnId, options);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "checkboxCustomFieldData", groups = "nightly-build")
	public void deleteContactCheckboxCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("checkbox", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "fileCustomFieldData", groups = "nightly-build")
	public void deleteContactFileCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("file", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "socialProfileCustomFieldData", groups = "nightly-build")
	public void deleteContactSocialProfileCustomField_Test(String fieldId, String fieldName, String columnId) {
		CustomFieldAlbatross requestData = createDeleteRequestData("social_profile", fieldName, columnId);
		
		String basePath = "custom-fields/delete/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Deleted Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
	}

	@DataProvider
	public Object[][] textCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("text", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] numberCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("number", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] dateCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("date", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] dateTimeCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("date_time", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] longTextCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("longtext", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] phoneCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("phonenumber", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] emailCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("email", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] dropdownCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, Object> fieldDataWithOptions = createCustomFieldWithOptionsAndReturnData("dropdown", fieldName);
		return new Object[][] { {fieldDataWithOptions.get("id"), fieldName, fieldDataWithOptions.get("columnid"), fieldDataWithOptions.get("options")} };
	}

	@DataProvider
	public Object[][] multiselectCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, Object> fieldDataWithOptions = createCustomFieldWithOptionsAndReturnData("multiselect", fieldName);
		return new Object[][] { {fieldDataWithOptions.get("id"), fieldName, fieldDataWithOptions.get("columnid"), fieldDataWithOptions.get("options")} };
	}

	@DataProvider
	public Object[][] checkboxCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("checkbox", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] fileCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("file", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	@DataProvider
	public Object[][] socialProfileCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		Map<String, String> fieldData = createCustomFieldAndReturnData("social_profile", fieldName);
		return new Object[][] { {fieldData.get("id"), fieldName, fieldData.get("columnid")} };
	}

	private CustomFieldAlbatross createDeleteRequestData(String fieldType, String fieldName, String columnId) {
		ExtraField extraField = new ExtraField();
		extraField.setColumnid(Integer.parseInt(columnId));
		extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDeleted(true);

		CustomFieldAlbatross deleteRequest = new CustomFieldAlbatross();
		deleteRequest.setCustumField(extraField);
		
		return deleteRequest;
	}

	private CustomFieldAlbatross createDeleteRequestDataWithOptions(String fieldType, String fieldName, String columnId, List<Map<String, Object>> options) {
		ExtraField extraField = new ExtraField();
		extraField.setColumnid(Integer.parseInt(columnId));
		extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDeleted(true);

		List<DefaultOptionsValue> defaultOptionsValue = new ArrayList<>();
		for (Map<String, Object> option : options) {
			DefaultOptionsValue optionValue = new DefaultOptionsValue();
			optionValue.setLabel((String) option.get("label"));
			optionValue.setSequence_no((Integer) option.get("sequence_no"));
			defaultOptionsValue.add(optionValue);
		}
		extraField.setDefaultoptionsvalue(defaultOptionsValue);

		CustomFieldAlbatross deleteRequest = new CustomFieldAlbatross();
		deleteRequest.setCustumField(extraField);
		
		return deleteRequest;
	}

	private Map<String, String> createCustomFieldAndReturnData(String fieldType, String fieldName) {
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
		JsonPath jsonPath = response.jsonPath();
		
		Map<String, String> fieldData = new HashMap<>();
		fieldData.put("id", jsonPath.getString("data.custumField.id"));
		fieldData.put("columnid", jsonPath.getString("data.custumField.columnid"));
		
		return fieldData;
	}

	private Map<String, Object> createCustomFieldWithOptionsAndReturnData(String fieldType, String fieldName) {
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> defaultOptionsValue = new ArrayList<>();
		DefaultOptionsValue option1 = new DefaultOptionsValue();
		option1.setLabel("Option 1");
		option1.setSequence_no(1);
		defaultOptionsValue.add(option1);

		DefaultOptionsValue option2 = new DefaultOptionsValue();
		option2.setLabel("Option 2");
		option2.setSequence_no(2);
		defaultOptionsValue.add(option2);

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		extraField.setDefaultoptionsvalue(defaultOptionsValue);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
		JsonPath jsonPath = response.jsonPath();
		
		Map<String, Object> fieldData = new HashMap<>();
		fieldData.put("id", jsonPath.getString("data.custumField.id"));
		fieldData.put("columnid", jsonPath.getString("data.custumField.columnid"));
		
		List<Map<String, Object>> options = new ArrayList<>();
		List<Map<String, Object>> responseOptions = jsonPath.getList("data.custumField.defaultoptionsvalue");
		for (Map<String, Object> option : responseOptions) {
			Map<String, Object> optionMap = new HashMap<>();
			optionMap.put("id", option.get("id"));
			optionMap.put("label", option.get("label"));
			optionMap.put("value", option.get("value"));
			optionMap.put("sequence_no", option.get("sequence_no"));
			options.add(optionMap);
		}
		fieldData.put("options", options);
		
		return fieldData;
	}
} 