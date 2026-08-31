package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
public class EditCompanyCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String apiAuthToken;
	private static final int COMPANY_ENTITY_TYPE_ID = 3;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "textCustomFieldData", groups = "nightly-build")
	public void editCompanyTextCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("text", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);
		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "text");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "numberCustomFieldData", groups = "nightly-build")
	public void editCompanyNumberCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("number", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "number");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dateCustomFieldData", groups = "nightly-build")
	public void editCompanyDateCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("date", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "date");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "dateTimeCustomFieldData", groups = "nightly-build")
	public void editCompanyDateTimeCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("date_time", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "date_time");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "longTextCustomFieldData", groups = "nightly-build")
	public void editCompanyLongTextCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("longtext", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "longtext");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "phoneCustomFieldData", groups = "nightly-build")
	public void editCompanyPhoneCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("phonenumber", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "phonenumber");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "dropdownCustomFieldData", groups = "nightly-build")
	public void editCompanyDropdownCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName, List<Map<String, Object>> existingOptions) {
		CustomFieldAlbatross requestData = createEditRequestData("dropdown", fieldId, accountId, originalFieldName, columnId, updatedFieldName, existingOptions);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "dropdown");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "multiselectCustomFieldData", groups = "nightly-build")
	public void editCompanyMultiselectCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName, List<Map<String, Object>> existingOptions) {
		CustomFieldAlbatross requestData = createEditRequestData("multiselect", fieldId, accountId, originalFieldName, columnId, updatedFieldName, existingOptions);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "multiselect");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "checkboxCustomFieldData", groups = "nightly-build")
	public void editCompanyCheckboxCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("checkbox", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "checkbox");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "fileCustomFieldData", groups = "nightly-build")
	public void editCompanyFileCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("file", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "file");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "socialProfileCustomFieldData", groups = "nightly-build")
	public void editCompanySocialProfileCustomField_Test(String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName) {
		CustomFieldAlbatross requestData = createEditRequestData("social_profile", fieldId, accountId, originalFieldName, columnId, updatedFieldName, null);
		
		String basePath = "custom-fields/" + fieldId;
		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, null, true, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), updatedFieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "social_profile");
	}

	private CustomFieldAlbatross createEditRequestData(String fieldType, String fieldId, String accountId, String originalFieldName, String columnId, String updatedFieldName, List<Map<String, Object>> existingOptions) {
		ExtraField extraField = new ExtraField();
		extraField.setExtrafieldtype(fieldType);
		extraField.setExtrafieldname(updatedFieldName);
		extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
		extraField.setColumnid(Integer.parseInt(columnId));

		if (fieldType.equals("dropdown") || fieldType.equals("multiselect")) {
			List<DefaultOptionsValue> updatedOptions = new ArrayList<>();
			String defaultValues = "";
			
			for (int i = 0; i < existingOptions.size(); i++) {
				Map<String, Object> existingOption = existingOptions.get(i);
				DefaultOptionsValue updatedOption = new DefaultOptionsValue();
				
				updatedOption.setLabel(existingOption.get("label") + "_Updated");
				updatedOption.setSequence_no((Integer) existingOption.get("sequence_no"));
				updatedOption.setTempId(UUID.randomUUID().toString());
				
				updatedOptions.add(updatedOption);
				
				if (i > 0) defaultValues += ",";
				defaultValues += existingOption.get("value");
			}
			
			extraField.setDefaultoptionsvalue(updatedOptions);
			extraField.setDefaultvalue(defaultValues);
		} else {
			extraField.setDefaultvalue(null);
		}

		CustomFieldAlbatross customField = new CustomFieldAlbatross();
		customField.setCustumField(extraField);

		return customField;
	}

	@DataProvider
	public Object[][] textCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("text", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] numberCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("number", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] dateCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("date", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] dateTimeCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("date_time", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] longTextCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("longtext", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] phoneCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("phonenumber", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] dropdownCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, Object> fieldDataWithOptions = createCustomFieldWithOptionsAndReturnData("dropdown", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldDataWithOptions.get("id"), fieldDataWithOptions.get("accountid"), originalFieldName, fieldDataWithOptions.get("columnid"), updatedFieldName, fieldDataWithOptions.get("options")} };
	}

	@DataProvider
	public Object[][] multiselectCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, Object> fieldDataWithOptions = createCustomFieldWithOptionsAndReturnData("multiselect", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldDataWithOptions.get("id"), fieldDataWithOptions.get("accountid"), originalFieldName, fieldDataWithOptions.get("columnid"), updatedFieldName, fieldDataWithOptions.get("options")} };
	}

	@DataProvider
	public Object[][] checkboxCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("checkbox", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] fileCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("file", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	@DataProvider
	public Object[][] socialProfileCustomFieldData() {
		String originalFieldName = faker.getCustomFieldName("companies");
		Map<String, String> fieldData = createCustomFieldAndReturnData("social_profile", originalFieldName);
		String updatedFieldName = faker.getCustomFieldName("companies") + "_Updated";
		return new Object[][] { {fieldData.get("id"), fieldData.get("accountid"), originalFieldName, fieldData.get("columnid"), updatedFieldName} };
	}

	private Map<String, String> createCustomFieldAndReturnData(String fieldType, String fieldName) {
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
		JsonPath jsonPath = response.jsonPath();
		
		Map<String, String> fieldData = new HashMap<>();
		fieldData.put("id", jsonPath.getString("data.custumField.id"));
		fieldData.put("accountid", jsonPath.getString("data.custumField.accountid"));
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
		extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		extraField.setDefaultoptionsvalue(defaultOptionsValue);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);
		JsonPath jsonPath = response.jsonPath();
		
		Map<String, Object> fieldData = new HashMap<>();
		fieldData.put("id", jsonPath.getString("data.custumField.id"));
		fieldData.put("accountid", jsonPath.getString("data.custumField.accountid"));
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