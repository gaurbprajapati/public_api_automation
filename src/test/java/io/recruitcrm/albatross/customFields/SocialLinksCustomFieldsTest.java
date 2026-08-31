package io.recruitcrm.albatross.customFields;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.albatross.JavaFakerCustomField;
import io.rcrm.api.pojo.albatross.CustomFieldAlbatross;
import io.rcrm.api.pojo.albatross.ExtraField;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class SocialLinksCustomFieldsTest extends TestBase {

	JavaFakerCustomField faker = new JavaFakerCustomField();
	String albatrossAuthToken;

	public SocialLinksCustomFieldsTest() {
		super();
	}

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getEntityData", groups = "nightly-build")
	public void createSocialLinksCustomFields_Test(String entityName, int entityId) {

		String customFieldName = faker.getCustomFieldName(entityName);
		String basePath = "custom-fields";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken, null, false,
				customField);
		Assert.assertEquals(response.getStatusCode(), 200);

		response.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotCreateSocialLinksCustomFields_Test() {

		String basePath = "custom-fields";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(faker.getRandomEntityId());
		extraField.setExtrafieldname(faker.getRandomCustomFieldName());
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossAuthToken + "123", null, false,
				customField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getEntityData", groups = "nightly-build")
	public void getSocialLinksCustomFields_Test(String entityName, int entityId) {

		String customFieldName = faker.getCustomFieldName(entityName);

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customField);
		Assert.assertEquals(response.getStatusCode(), 200);

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(entityId));

		String basePath = "custom-fields/social-links/{id}";

		ExtraField extraField1 = new ExtraField();
		CustomFieldAlbatross customField1 = new CustomFieldAlbatross();

		extraField1.setColumnid(faker.getColumnId());
		extraField1.setEntitytypeid(entityId);
		extraField1.setExtrafieldname("Updated " + customFieldName);
		extraField1.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField1.setExtrafieldtype("social_profile");
		customField1.setCustumField(extraField1);

		Response response1 = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true);

		Assert.assertEquals(response1.getStatusCode(), 200);

		response1.then().body("data.social_link_columns[0]", Matchers.notNullValue());
		response1.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetSocialLinksCustomFields_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(faker.getRandomEntityId()));

		String basePath = "custom-fields/social-links/{id}";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, albatrossAuthToken + "123", null,
				pathParamters, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getEntityData", groups = "nightly-build")
	public void editSocialLinksCustomFields_Test(String entityName, int entityId) {

		String customFieldName = faker.getCustomFieldName(entityName);

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customField);
		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int customFieldId = json.get("data.custumField.id");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(customFieldId));

		String basePath = "custom-fields/{id}";

		Response response1 = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true, customField);

		Assert.assertEquals(response1.getStatusCode(), 200);

		response1.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response1.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotEditSocialLinksCustomFields_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(faker.getRandomEntityId()));

		String basePath = "custom-fields/{id}";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(faker.getRandomEntityId());
		extraField.setExtrafieldname(faker.getRandomCustomFieldName());
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken + "123", null,
				pathParamters, false, customField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getEntityData", groups = "nightly-build")
	public void deleteSocialLinksCustomFields_Test(String entityName, int entityId) {

		String customFieldName = faker.getCustomFieldName(entityName);

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(entityId);
		extraField.setExtrafieldname(customFieldName);
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost("JSON", albatrossURL, "custom-fields", albatrossAuthToken, null, false,
				customField);
		Assert.assertEquals(response.getStatusCode(), 200);

		JsonPath json = response.jsonPath();
		int customFieldId = json.get("data.custumField.id");

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(customFieldId));

		String basePath = "custom-fields/{id}";

		ExtraField extraField1 = new ExtraField();
		CustomFieldAlbatross customField1 = new CustomFieldAlbatross();

		extraField1.setColumnid(faker.getColumnId());
		extraField1.setEntitytypeid(entityId);
		extraField1.setExtrafieldname(customFieldName);
		extraField1.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField1.setExtrafieldtype("social_profile");
		extraField1.setDeleted(true);
		customField1.setCustumField(extraField1);

		Response response1 = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken, null, pathParamters,
				true, customField);

		Assert.assertEquals(response1.getStatusCode(), 200);

		response1.then().body("message", Matchers.is("Custom Field Saved Successfully"));
		response1.then().body("message_type", Matchers.is("is-success"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDeleteSocialLinksCustomFields_Test() {

		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("id", String.valueOf(faker.getRandomEntityId()));

		String basePath = "custom-fields/{id}";

		ExtraField extraField = new ExtraField();
		CustomFieldAlbatross customField = new CustomFieldAlbatross();

		extraField.setColumnid(faker.getColumnId());
		extraField.setEntitytypeid(faker.getRandomEntityId());
		extraField.setExtrafieldname(faker.getRandomCustomFieldName());
		extraField.setDefaultvalue(faker.getDefaultvalue("social links"));
		extraField.setExtrafieldtype("social_profile");
		customField.setCustumField(extraField);

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, albatrossAuthToken + "123", null,
				pathParamters, false, customField);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getEntityData() {

		Object data[][] = { { "candidates", 5 }, { "contacts", 2 }, { "companies", 3 }, { "jobs", 4 },
				{ "deals", 11 } };

		return data;
	}

}