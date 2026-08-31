package io.recruitcrm.albatross.customFields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import io.rcrm.api.pojo.albatross.NestedCustomFieldPojo;
import io.rcrm.api.pojo.albatross.UnlinkNestedCustomFieldPojo;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class NestedCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String apiAuthToken;
	String albatrossAuthToken;
	String invalidAuthToken;

	public NestedCustomFieldsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
		invalidAuthToken = ThreadManager.getOwnerAlbatrossToken() + "123";
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCustomFieldData", groups = "nightly-build")
	public void createNestedCustomFields_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		mapping1.setParent_value_id(parentOptionId);
		mapping1.setChild_value_id(childOptionId1);
		mapping1.setChild_visibility(null);
		mappingsList.add(mapping1);

		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		mapping2.setParent_value_id(parentOptionId);
		mapping2.setChild_value_id(childOptionId2);
		mapping2.setChild_visibility(null);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setEntity(String.valueOf(entityId));
		nestedCustomField.setLevel(1);
		nestedCustomField.setDependency_id(String.valueOf(parentCustomFieldId));
		nestedCustomField.setParent_id(parentCustomFieldId);
		nestedCustomField.setChild_id(childCustomFieldId);
		nestedCustomField.setMappings(mappingsList);

		String basePath = "nested-custom-fields/store";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Field dependency added successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNestedCustomFieldsWithEmptyRequest_Test() {

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();
		mappingsList.add(mapping1);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setMappings(mappingsList);

		String basePath = "nested-custom-fields/store";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("message",
				Matchers.is("The entity field is required.,The dependency id field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotCreateNestedCustomFields_Test() {

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();

		String basePath = "nested-custom-fields/store";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, invalidAuthToken, null, false,
				nestedCustomField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void editNestedCustomFields_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		mapping1.setParent_value_id(parentOptionId);
		mapping1.setChild_value_id(childOptionId1);
		mapping1.setChild_visibility(null);
		mappingsList.add(mapping1);

		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		mapping2.setParent_value_id(parentOptionId);
		mapping2.setChild_value_id(childOptionId2);
		mapping2.setChild_visibility(null);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setEntity(String.valueOf(entityId));
		nestedCustomField.setLevel(1);
		nestedCustomField.setDependency_id(String.valueOf(parentCustomFieldId));
		nestedCustomField.setParent_id(parentCustomFieldId);
		nestedCustomField.setChild_id(childCustomFieldId);
		nestedCustomField.setMappings(mappingsList);

		String basePath = "nested-custom-fields/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Field dependency edited successfully "));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void editNestedCustomFieldsWithEmptyRequest_Test() {

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();
		mappingsList.add(mapping1);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setMappings(mappingsList);

		String basePath = "nested-custom-fields/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);

		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("message",
				Matchers.is("The entity field is required.,The dependency id field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditNestedCustomFields_Test() {

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();

		String basePath = "nested-custom-fields/update";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, invalidAuthToken, null, false,
				nestedCustomField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void getNestedCustomFields_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(entityId));

		String basePath = "nested-custom-fields/get/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("NestedCustomFields Dependency Data"));
		response.then().body("message_type", Matchers.is("is-success"));

		Map<String, Object> responseData = response.jsonPath().getMap("data[0]");
		Assert.assertEquals(responseData.keySet().iterator().next(), String.valueOf(parentCustomFieldId));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getNestedCustomFieldsWithEmptyRequest_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));

		String basePath = "nested-custom-fields/get/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("NestedCustomFields Dependency Data"));
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath json = response.jsonPath();
		Assert.assertTrue(json.getList("data").isEmpty(), "Data is reflecting even though there is no dependency");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetNestedCustomFields_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));

		String basePath = "nested-custom-fields/get/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, invalidAuthToken, null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void unlinkNestedCustomFields_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		UnlinkNestedCustomFieldPojo nestedCustomField = new UnlinkNestedCustomFieldPojo();
		nestedCustomField.setEntity(String.valueOf(entityId));
		nestedCustomField.setParent_id(parentCustomFieldId);
		nestedCustomField.setChild_id(childCustomFieldId);

		String basePath = "nested-custom-fields/unlink";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Field dependency unlinked successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unlinkNestedCustomFieldsWithEmptyRequest_Test() {

		UnlinkNestedCustomFieldPojo nestedCustomField = new UnlinkNestedCustomFieldPojo();

		String basePath = "nested-custom-fields/unlink";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 422);

		response.then().body("message", Matchers.is("The entity field is required."));
		response.then().body("message_type", Matchers.is("is-danger"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotUnlinkNestedCustomFields_Test() {

		UnlinkNestedCustomFieldPojo nestedCustomField = new UnlinkNestedCustomFieldPojo();

		String basePath = "nested-custom-fields/unlink";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, invalidAuthToken, null, false,
				nestedCustomField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getCustomFieldData", groups = "nightly-build")
	public void getDefaultOptions_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(entityId));

		String basePath = "custom-fields/get-default-options/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Default options for entity custom fields"));
		response.then().body("message_type", Matchers.is("is-success"));

		Map<String, Object> responseData = response.jsonPath().getMap("data");
		Iterator<String> keyIterator = responseData.keySet().iterator();
		Assert.assertEquals(keyIterator.next(), String.valueOf(parentCustomFieldId));
		Assert.assertEquals(keyIterator.next(), String.valueOf(childCustomFieldId));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getDefaultOptionsWithEmptyRequest_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));

		String basePath = "custom-fields/get-default-options/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Default options for entity custom fields"));
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath json = response.jsonPath();
		Assert.assertTrue(json.getList("data").isEmpty(), "Options are displaying for empty state");
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetDefaultOptions_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));

		String basePath = "custom-fields/get-default-options/{entity}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, invalidAuthToken, null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void canDeleteNestedCustomField_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(entityId));
		pathParamters.put("custom_field_id", String.valueOf(parentCustomFieldId));

		String basePath = "custom-fields/can-delete/{entity}/{custom_field_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("This field is involved in dependency"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.can_delete_custom_field", Matchers.is(false));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void canDeleteNestedCustomFieldWithEmptyRequest_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));
		pathParamters.put("custom_field_id", String.valueOf(faker.getEntityId()));

		String basePath = "custom-fields/can-delete/{entity}/{custom_field_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("This field is not involved in dependency"));
		response.then().body("message_type", Matchers.is("is-success"));
		response.then().body("data.can_delete_custom_field", Matchers.is(true));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void unauthorizedUserCannotDeleteNestedCustomField_Test(int entityId, int parentCustomFieldId,
			int childCustomFieldId, int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(entityId));
		pathParamters.put("custom_field_id", String.valueOf(parentCustomFieldId));

		String basePath = "custom-fields/can-delete/{entity}/{custom_field_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, invalidAuthToken, null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void canDeleteNestedCustomFieldOption_Test(int entityId, int parentCustomFieldId, int childCustomFieldId,
			int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(parentOptionId));
		pathParamters.put("custom_field_option_id", String.valueOf(parentCustomFieldId));

		String basePath = "custom-fields/can-delete/options/{entity}/{custom_field_option_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Deletable options"));
		response.then().body("message_type", Matchers.is("is-success"));

		List<Integer> deletableOptionIds = response.jsonPath().getList("data.deletable_option_ids", Integer.class);
		Assert.assertFalse(deletableOptionIds.contains(parentOptionId), "Able to delete " + parentOptionId);
		Assert.assertFalse(deletableOptionIds.contains(childOptionId1), "Able to delete " + childOptionId1);
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void canDeleteNestedCustomFieldOptionWithEmptyRequest_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(faker.getEntityId()));
		pathParamters.put("custom_field_option_id", String.valueOf(faker.getEntityId()));

		String basePath = "custom-fields/can-delete/options/{entity}/{custom_field_option_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Deletable options"));
		response.then().body("message_type", Matchers.is("is-success"));

		JsonPath json = response.jsonPath();
		Assert.assertTrue(json.getList("data.deletable_option_ids").isEmpty(),
				"Options are displaying for empty state");
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getNestedCustomFieldData", groups = "nightly-build")
	public void unauthorizedUserCannotDeleteNestedCustomFieldOptions_Test(int entityId, int parentCustomFieldId,
			int childCustomFieldId, int parentOptionId, int childOptionId1, int childOptionId2) {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("entity", String.valueOf(parentOptionId));
		pathParamters.put("custom_field_option_id", String.valueOf(parentCustomFieldId));

		String basePath = "custom-fields/can-delete/options/{entity}/{custom_field_option_id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, invalidAuthToken, null, pathParamters,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getCustomFieldData() {

		int entityId = faker.getValidEntityId();
		String entityName = faker.getEntityName(entityId);

		// create parent custom field of dropdown type
		String parentCustomFieldName = "Parent " + faker.getCustomFieldName(entityName);
		String parentCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		ExtraField parentExtraField = new ExtraField();
		CustomFieldAlbatross parentCustomField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> parentOptionsList = new ArrayList<>();
		String[] parentOptions = parentCustomFieldOptions.split(",");
		for (int i = 0; i < parentOptions.length; i++) {
			DefaultOptionsValue parentOption = new DefaultOptionsValue();
			parentOption.setLabel(parentOptions[i].trim());
			parentOption.setSequence_no(i + 1);
			parentOption.setTempId(faker.getTempId());
			parentOptionsList.add(parentOption);
		}
		parentExtraField.setDefaultoptionsvalue(parentOptionsList);

		parentExtraField.setColumnid(1);
		parentExtraField.setEntitytypeid(entityId);
		parentExtraField.setExtrafieldname(parentCustomFieldName);
		parentExtraField.setExtrafieldtype("dropdown");
		parentCustomField.setCustumField(parentExtraField);

		Response parentResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null,
				false, parentCustomField);

		Assert.assertEquals(parentResponse.getStatusCode(), 200);

		JsonPath parentJson = parentResponse.jsonPath();
		int parentCustomFieldId = parentJson.get("data.custumField.id");
		int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

		// create child custom field of multiselect type
		String childCustomFieldName = "Child " + faker.getCustomFieldName(entityName);
		String childCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		ExtraField childExtraField = new ExtraField();
		CustomFieldAlbatross childCustomField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> childOptionsList = new ArrayList<>();
		String[] childOptions = childCustomFieldOptions.split(",");
		for (int i = 0; i < childOptions.length; i++) {
			DefaultOptionsValue childOption = new DefaultOptionsValue();
			childOption.setLabel(childOptions[i].trim());
			childOption.setSequence_no(i + 1);
			childOption.setTempId(faker.getTempId());
			childOptionsList.add(childOption);
		}
		childExtraField.setDefaultoptionsvalue(childOptionsList);

		childExtraField.setColumnid(2);
		childExtraField.setEntitytypeid(entityId);
		childExtraField.setExtrafieldname(childCustomFieldName);
		childExtraField.setExtrafieldtype("multiselect");
		childCustomField.setCustumField(childExtraField);

		Response childResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null,
				false, childCustomField);

		Assert.assertEquals(childResponse.getStatusCode(), 200);

		JsonPath childJson = childResponse.jsonPath();

		int childCustomFieldId = childJson.get("data.custumField.id");
		int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
		int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

		Object data[][] = {
				{ entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2 } };

		return data;
	}

	@DataProvider(parallel = true)
	public Object[][] getNestedCustomFieldData() {

		int entityId = faker.getValidEntityId();
		String entityName = faker.getEntityName(entityId);

		// create parent custom field of dropdown type
		String parentCustomFieldName = "Parent " + faker.getCustomFieldName(entityName);
		String parentCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		ExtraField parentExtraField = new ExtraField();
		CustomFieldAlbatross parentCustomField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> parentOptionsList = new ArrayList<>();
		String[] parentOptions = parentCustomFieldOptions.split(",");
		for (int i = 0; i < parentOptions.length; i++) {
			DefaultOptionsValue parentOption = new DefaultOptionsValue();
			parentOption.setLabel(parentOptions[i].trim());
			parentOption.setSequence_no(i + 1);
			parentOption.setTempId(faker.getTempId());
			parentOptionsList.add(parentOption);
		}
		parentExtraField.setDefaultoptionsvalue(parentOptionsList);

		parentExtraField.setColumnid(1);
		parentExtraField.setEntitytypeid(entityId);
		parentExtraField.setExtrafieldname(parentCustomFieldName);
		parentExtraField.setExtrafieldtype("dropdown");
		parentCustomField.setCustumField(parentExtraField);

		Response parentResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null,
				false, parentCustomField);

		Assert.assertEquals(parentResponse.getStatusCode(), 200);

		JsonPath parentJson = parentResponse.jsonPath();
		int parentCustomFieldId = parentJson.get("data.custumField.id");
		int parentOptionId = parentJson.get("data.custumField.defaultoptionsvalue[0].id");

		// create child custom field of multiselect type
		String childCustomFieldName = "Child " + faker.getCustomFieldName(entityName);
		String childCustomFieldOptions = faker.getNumberOfDefaultOptionsValues(3);

		ExtraField childExtraField = new ExtraField();
		CustomFieldAlbatross childCustomField = new CustomFieldAlbatross();

		List<DefaultOptionsValue> childOptionsList = new ArrayList<>();
		String[] childOptions = childCustomFieldOptions.split(",");
		for (int i = 0; i < childOptions.length; i++) {
			DefaultOptionsValue childOption = new DefaultOptionsValue();
			childOption.setLabel(childOptions[i].trim());
			childOption.setSequence_no(i + 1);
			childOption.setTempId(faker.getTempId());
			childOptionsList.add(childOption);
		}
		childExtraField.setDefaultoptionsvalue(childOptionsList);

		childExtraField.setColumnid(2);
		childExtraField.setEntitytypeid(entityId);
		childExtraField.setExtrafieldname(childCustomFieldName);
		childExtraField.setExtrafieldtype("multiselect");
		childCustomField.setCustumField(childExtraField);

		Response childResponse = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null,
				false, childCustomField);

		Assert.assertEquals(childResponse.getStatusCode(), 200);

		JsonPath childJson = childResponse.jsonPath();

		int childCustomFieldId = childJson.get("data.custumField.id");
		int childOptionId1 = childJson.get("data.custumField.defaultoptionsvalue[0].id");
		int childOptionId2 = childJson.get("data.custumField.defaultoptionsvalue[1].id");

		// create dependency between dropdown and multiselect field
		List<NestedCustomFieldPojo.Mapping> mappingsList = new ArrayList<>();

		NestedCustomFieldPojo.Mapping mapping1 = new NestedCustomFieldPojo.Mapping();
		mapping1.setParent_value_id(parentOptionId);
		mapping1.setChild_value_id(childOptionId1);
		mapping1.setChild_visibility(null);
		mappingsList.add(mapping1);

		NestedCustomFieldPojo.Mapping mapping2 = new NestedCustomFieldPojo.Mapping();

		mapping2.setParent_value_id(parentOptionId);
		mapping2.setChild_value_id(childOptionId2);
		mapping2.setChild_visibility(null);
		mappingsList.add(mapping2);

		NestedCustomFieldPojo nestedCustomField = new NestedCustomFieldPojo();
		nestedCustomField.setEntity(String.valueOf(entityId));
		nestedCustomField.setLevel(1);
		nestedCustomField.setDependency_id(String.valueOf(parentCustomFieldId));
		nestedCustomField.setParent_id(parentCustomFieldId);
		nestedCustomField.setChild_id(childCustomFieldId);
		nestedCustomField.setMappings(mappingsList);

		Response response = RestClient.doPost("JSON", albatrossURL, "nested-custom-fields/store", albatrossAuthToken,
				null, false, nestedCustomField);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Field dependency added successfully"));
		response.then().body("message_type", Matchers.is("is-success"));

		Object data[][] = {
				{ entityId, parentCustomFieldId, childCustomFieldId, parentOptionId, childOptionId1, childOptionId2 } };

		return data;
	}

}