package io.recruitcrm.albatross.customFields;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetRequiredFieldsOnExtensionTest extends TestBase {

	public GetRequiredFieldsOnExtensionTest() {
		super();
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postGetRequiredFieldsOnExtension_200() {
		Response response = RestClient.doPost("JSON", albatrossURL, "get-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken(), null, true, null);


		Assert.assertEquals(response.getStatusCode(), 200, "Endpoint failure");
		response.then().body("message_type", Matchers.containsString("is_success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath(
				"privateApi//enforceMandatoryFieldsOnExtension//getRequiredFieldsOnExtension.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postGetRequiredFieldsOnExtensionWithInvallidEndpoint_404() {
		Response response = RestClient.doPost("JSON", albatrossURL, "get-required-fields-on-extension123",
				ThreadManager.getOwnerAlbatrossToken(), null, true, null);


		Assert.assertEquals(response.getStatusCode(), 404, "Endpoint failure");
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUnauthorizedUserCannotGetRequiredFieldsOnExtension_401() {
		Response response = RestClient.doPost("JSON", albatrossURL, "get-required-fields-on-extension",
				ThreadManager.getOwnerAlbatrossToken() + "x001", null, true, null);

		Assert.assertEquals(response.getStatusCode(), 401, "Endpoint failure");
		response.then().body("error", Matchers.containsString("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));

	}

}
