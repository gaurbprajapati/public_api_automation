package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
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
public class DropdownMultiselectCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;
	String apiAuthToken;

	public DropdownMultiselectCustomFieldsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createDropdownMultiselectCustomFields_Test() {
		int entityId = faker.getEntityId();
		String entityName = faker.getEntityName(entityId);
		String customFieldName = faker.getCustomFieldName(entityName);
		String customFieldType = faker.getFieldType();
		String customFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		String basePath = "custom-fields";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

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

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(customFieldType);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				customField);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
		response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
		// assert sequence of options in both defaultvalue and defaultoptionsvalue
		response.then().body("data.custumField.defaultvalue", Matchers.is(customFieldOptions.replaceAll(",\\s+", ",")));
		response.then().body("data.custumField.defaultoptionsvalue.size()", Matchers.is(3));
		for (int i = 0; i < options.length; i++) {
			response.then().body("data.custumField.defaultoptionsvalue[" + i + "].label",
					Matchers.is(options[i].trim()));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCustomFieldData", groups = "nightly-build")
	public void getDropdownMultiselectCustomFields_Test(int customFieldId, int entityId, String entityName,
			String fieldType, String customFieldOptions, String customFieldName) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("entitytypeid", String.valueOf(entityId));

		String basePath = "custom-fields/get";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, queryParameters,
				null, true, null);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message_type", Matchers.is("is_success"));
		response.then().body("data.custumFields[0].entitytypeid", Matchers.is(entityId));
		response.then().body("data.custumFields[0].extrafieldname", Matchers.is(customFieldName));
		// assert sequence of options in both defaultvalue and defaultoptionsvalue
		response.then().body("data.custumFields[0].defaultvalue",
				Matchers.is(customFieldOptions.replaceAll(",\\s+", ",")));
		response.then().body("data.custumFields[0].defaultoptionsvalue.size()", Matchers.is(3));
		String[] options = customFieldOptions.split(",");
		for (int i = 0; i < options.length; i++) {
			response.then().body("data.custumFields[0].defaultoptionsvalue[" + i + "].label",
					Matchers.is(options[i].trim()));
		}
		
		// verify created custom fields displayed in response of entity custom fields  public api end point
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", entityName);

		Response response1 = RestClient.doGet("JSON", baseURL, "custom-fields/{entity}", apiAuthToken, null,
				pathParamters, true);

		Assert.assertEquals(response1.getStatusCode(), 200);

		response1.then().body("[0].field_type", Matchers.is(fieldType));
		response1.then().body("[0].field_name", Matchers.is(customFieldName));
		response1.then().body("[0].default_value", Matchers.is(customFieldOptions.replaceAll(",\\s+", ",")));
		
		// verify created custom fields displayed in response of all custom fields  public api end point
		Response response2 = RestClient.doGet("JSON", baseURL, "custom-fields", apiAuthToken, null, null, true);

		Assert.assertEquals(response2.getStatusCode(), 200);

		response2.then().body("[0].field_type", Matchers.is(fieldType));
		response2.then().body("[0].field_name", Matchers.is(customFieldName));
		response2.then().body("[0].default_value", Matchers.is(customFieldOptions.replaceAll(",\\s+", ",")));

	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCustomFieldData", groups = "nightly-build")
	public void editDropdownMultiselectCustomFields_Test(int customFieldId, int entityId, String entityName,
			String fieldType, String fieldOptions, String fieldName) {

		String customFieldName = faker.getCustomFieldName(entityName);
		String customFieldType = faker.getFieldType();
		String customFieldOptions = faker.getNumberOfDefaultOptionsValues(5);

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

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

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(customFieldType);
		customField.setCustumField(extraField);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(customFieldId));

		String basePath = "custom-fields/{id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true, customField);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.custumField.entitytypeid", Matchers.is(entityId));
		response.then().body("data.custumField.extrafieldname", Matchers.is(customFieldName));
		// assert updated sequence of options in both defaultvalue and defaultoptionsvalue
		response.then().body("data.custumField.defaultvalue", Matchers.is(customFieldOptions.replaceAll(",\\s+", ",")));
		response.then().body("data.custumField.defaultoptionsvalue.size()", Matchers.is(5));
		for (int i = 0; i < options.length; i++) {
			response.then().body("data.custumField.defaultoptionsvalue[" + i + "].label",
					Matchers.is(options[i].trim()));
		}
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCustomFieldData", groups = "nightly-build")
	public void deleteDropdownMultiselectCustomFields_Test(int customFieldId, int entityId, String entityName,
			String customFieldName, String customFieldOptions, String customFieldType) {

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

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

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(customFieldType);
		extraField.setDeleted(true);
		customField.setCustumField(extraField);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(customFieldId));

		String basePath = "custom-fields/delete/{id}";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true, customField);

		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Custom Field Deleted Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
		// assert deleted options in both defaultvalue and defaultoptionsvalue
		response.then().body("data.custumField", Matchers.nullValue());
	}

	@DataProvider(parallel = true)
	public Object[][] getCustomFieldData() {

		int entityId = faker.getEntityId();
		String entityName = faker.getEntityName(entityId);
		String customFieldName = faker.getCustomFieldName(entityName);
		String customFieldType = faker.getFieldType();
		String customFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

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

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setExtrafieldtype(customFieldType);
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customField);

		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int customFieldId = json.get("data.custumField.id");

		Object data[][] = {
				{ customFieldId, entityId, entityName, customFieldType, customFieldOptions, customFieldName } };

		return data;
	}

}