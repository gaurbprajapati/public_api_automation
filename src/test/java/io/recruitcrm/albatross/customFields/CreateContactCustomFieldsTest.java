package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.List;

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
public class CreateContactCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String apiAuthToken;
	private static final int CONTACT_ENTITY_TYPE_ID = 2;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "textCustomFieldData", groups = "nightly-build")
	public void createContactTextCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("text", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "text");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "numberCustomFieldData", groups = "nightly-build")
	public void createContactNumberCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("number", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "number");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "dateCustomFieldData", groups = "nightly-build")
	public void createContactDateCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("date", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "date");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "dateTimeCustomFieldData", groups = "nightly-build")
	public void createContactDateTimeCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("date_time", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "date_time");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "longTextCustomFieldData", groups = "nightly-build")
	public void createContactLongTextCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("longtext", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "longtext");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "phoneCustomFieldData", groups = "nightly-build")
	public void createContactPhoneCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("phonenumber", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "phonenumber");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "emailCustomFieldData", groups = "nightly-build")
	public void createContactEmailCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("email", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "email");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "dropdownCustomFieldData", groups = "nightly-build")
	public void createContactDropdownCustomField_Test(String fieldName, List<DefaultOptionsValue> options) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("dropdown", fieldName, options);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "dropdown");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "multiselectCustomFieldData", groups = "nightly-build")
	public void createContactMultiselectCustomField_Test(String fieldName, List<DefaultOptionsValue> options) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("multiselect", fieldName, options);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "multiselect");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "checkboxCustomFieldData", groups = "nightly-build")
	public void createContactCheckboxCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("checkbox", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "checkbox");
	}

	@Owner("Akshaya Uppala")
	@Test(dataProvider = "fileCustomFieldData", groups = "nightly-build")
	public void createContactFileCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("file", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "file");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "socialProfileCustomFieldData", groups = "nightly-build")
	public void createContactSocialProfileCustomField_Test(String fieldName) {
		CustomFieldAlbatross requestData = createCustomFieldRequest("social_profile", fieldName, null);
		
		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, requestData);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), CONTACT_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), "social_profile");
	}

	@DataProvider
	public Object[][] textCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] numberCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] dateCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] dateTimeCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] longTextCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] phoneCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] emailCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] dropdownCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		List<DefaultOptionsValue> options = createDefaultOptions();
		return new Object[][] { {fieldName, options} };
	}

	@DataProvider
	public Object[][] multiselectCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		List<DefaultOptionsValue> options = createDefaultOptions();
		return new Object[][] { {fieldName, options} };
	}

	@DataProvider
	public Object[][] checkboxCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] fileCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	@DataProvider
	public Object[][] socialProfileCustomFieldData() {
		String fieldName = faker.getCustomFieldName("contacts");
		return new Object[][] { {fieldName} };
	}

	private CustomFieldAlbatross createCustomFieldRequest(String fieldType, String fieldName, List<DefaultOptionsValue> options) {
		ExtraField extraField = new ExtraField();
		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(CONTACT_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		
		if (options != null) {
			extraField.setDefaultoptionsvalue(options);
		}

		CustomFieldAlbatross customField = new CustomFieldAlbatross();
		customField.setCustumField(extraField);
		
		return customField;
	}

	private List<DefaultOptionsValue> createDefaultOptions() {
		List<DefaultOptionsValue> options = new ArrayList<>();
		
		DefaultOptionsValue option1 = new DefaultOptionsValue();
		option1.setLabel("Option 1");
		option1.setSequence_no(1);
		options.add(option1);

		DefaultOptionsValue option2 = new DefaultOptionsValue();
		option2.setLabel("Option 2");
		option2.setSequence_no(2);
		options.add(option2);

		return options;
	}
} 