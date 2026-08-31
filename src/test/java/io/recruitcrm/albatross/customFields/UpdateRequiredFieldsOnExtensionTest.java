package io.recruitcrm.albatross.customFields;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerCandidate;
import io.rcrm.api.pojo.UpdateFieldsOnExtension;
import io.rcrm.api.pojo.Value;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class UpdateRequiredFieldsOnExtensionTest extends TestBase {
	
	JavaFakerCandidate fakerCandidate = new JavaFakerCandidate();
	String key = "required_on_extension";
	String randomString = "x001";

	public UpdateRequiredFieldsOnExtensionTest() {
		super();
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtension_200() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getValidEntityTypeId();

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 200, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().body("message", Matchers.containsString("Field Updated Successfully"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//updateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithInvalidEntityId_422() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getInvalidEntityTypeId();

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 422, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The selected entitytypeid is invalid."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//invalidValuesInUpdateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithInvalidKey_422() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getValidEntityTypeId();

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key + randomString, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 422, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The selected key is invalid"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//invalidValuesInUpdateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithNullEntityId_422() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = 0;

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 422, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The selected entitytypeid is invalid."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//invalidValuesInUpdateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithNullKey_422() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getValidEntityTypeId();

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(null, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 422, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The key field is required."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//invalidValuesInUpdateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithNullValue_422() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getValidEntityTypeId();

		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key, null, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 422, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is-danger"));
		response.then().body("message", Matchers.containsString("The value field is required."));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//invalidValuesInUpdateFieldsOnExtension.json"));

	}
	
	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUpdateRequiredFieldsOnExtensionWithUnauthorizedUser_401() {

		boolean toggleState = fakerCandidate.getRandomToggleState();
		boolean candidateState = fakerCandidate.getRandomToggleState();
		boolean companyState = fakerCandidate.getRandomToggleState();
		boolean contactState = fakerCandidate.getRandomToggleState();
		int entityTypeId = fakerCandidate.getValidEntityTypeId();

		Value value = new Value(candidateState, companyState, contactState);
		UpdateFieldsOnExtension updateFieldsOnExtension = new UpdateFieldsOnExtension(key, value, entityTypeId,
				toggleState);
		Response response = RestClient.doPost("JSON", albatrossURL, "update-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken() + randomString, null, true, updateFieldsOnExtension);


		Assert.assertEquals(response.getStatusCode(), 401, "Endpoint failure");
		response.then().body("error", Matchers.containsString("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));

	}

}
