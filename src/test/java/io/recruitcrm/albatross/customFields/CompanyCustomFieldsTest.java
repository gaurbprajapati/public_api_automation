package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
public class CompanyCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String apiAuthToken;
	private static final int COMPANY_ENTITY_TYPE_ID = 3;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createCompanyTextCustomField_Test() {
		createCompanyCustomFieldHelper("text");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanyNumberCustomField_Test() {
		createCompanyCustomFieldHelper("number");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void createCompanyDateCustomField_Test() {
		createCompanyCustomFieldHelper("date");
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createCompanyDateTimeCustomField_Test() {
		createCompanyCustomFieldHelper("date_time");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanyLongTextCustomField_Test() {
		createCompanyCustomFieldHelper("longtext");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void createCompanyPhoneCustomField_Test() {
		createCompanyCustomFieldHelper("phonenumber");
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createCompanyDropdownCustomField_Test() {
		createCompanyCustomFieldWithOptionsHelper("dropdown");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanyMultiselectCustomField_Test() {
		createCompanyCustomFieldWithOptionsHelper("multiselect");
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void createCompanyCheckboxCustomField_Test() {
		createCompanyCustomFieldHelper("checkbox");
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void createCompanyFileCustomField_Test() {
		createCompanyCustomFieldHelper("file");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createCompanySocialProfileCustomField_Test() {
		createCompanyCustomFieldHelper("social_profile");
	}

	private void createCompanyCustomFieldHelper(String fieldType) {
		String fieldName = faker.getCustomFieldName("companies");
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		extraField.setDefaultvalue(null);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), fieldType);
	}

	private void createCompanyCustomFieldWithOptionsHelper(String fieldType) {
		String fieldName = faker.getCustomFieldName("companies");
		String fieldOptions = "Option1,Option2,Option3";
		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> optionsList = new ArrayList<>();
		String[] options = fieldOptions.split(",");
		for (int i = 0; i < options.length; i++) {
			DefaultOptionsValue option = new DefaultOptionsValue();
			option.setLabel(options[i].trim());
			option.setSequence_no(i + 1);
			option.setTempId(faker.getTempId());
			optionsList.add(option);
		}
		extraField.setDefaultoptionsvalue(optionsList);

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(COMPANY_ENTITY_TYPE_ID);
		extraField.setExtrafieldname(fieldName);
		extraField.setExtrafieldtype(fieldType);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false, customField);

		Assert.assertEquals(response.getStatusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals(jsonPath.getString("message"), "Custom Field Saved Successfully");
		Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
		Assert.assertEquals(jsonPath.getInt("data.custumField.entitytypeid"), COMPANY_ENTITY_TYPE_ID);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldname"), fieldName);
		Assert.assertEquals(jsonPath.getString("data.custumField.extrafieldtype"), fieldType);
		Assert.assertEquals(jsonPath.getString("data.custumField.defaultvalue"), fieldOptions.replaceAll(",\\s+", ","));
	}

} 