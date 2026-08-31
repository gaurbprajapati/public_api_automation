package io.recruitcrm.albatross.emailTriggers;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerTrigger;
import io.rcrm.api.pojo.albatross.emailtrigger.Emailtriggersetting;
import io.rcrm.api.pojo.albatross.emailtrigger.NewEmailTrigger;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CreateEmailTrigger_Test extends TestBase {

	String albatrossToken = "";

	@BeforeClass(alwaysRun = true)	public void getAccountAPI() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
	}

	JavaFakerTrigger fakerTrigger = new JavaFakerTrigger();

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void postCreateEmailTriggerTest_200(int trigger, int stageId, String name, String assertionMsg) {

		Emailtriggersetting emailtriggersetting = new Emailtriggersetting(fakerTrigger.getTriggerName(), trigger,
				stageId, 0);
		NewEmailTrigger newEmailTrigger = new NewEmailTrigger(emailtriggersetting);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-triggers", albatrossToken, null, true,
				newEmailTrigger);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertEquals("Response body differs", jsonPath.get("message"), "Add Email Trigger Successful ");
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
	}

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void postCreateEmailTriggerWithInvalidValuesTest_422(int trigger, int stageId, String name,
			String assertionMsg) {

		Emailtriggersetting emailtriggersetting = new Emailtriggersetting(name, fakerTrigger.getTriggerId(), stageId,
				0);
		NewEmailTrigger newEmailTrigger = new NewEmailTrigger(emailtriggersetting);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-triggers", albatrossToken, null, true,
				newEmailTrigger);
		Assert.assertEquals("Request Failure", response.statusCode(), 422);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertTrue("Response body differs", jsonPath.get("message").toString().contains(assertionMsg));
		response.then().body("status", Matchers.containsString("fail"));
		response.then().body("message_type", Matchers.containsString("is-danger"));
	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void postUnauthorizedAccessCreateEmailTriggerTest_401() {

		Emailtriggersetting emailtriggersetting = new Emailtriggersetting(null, 0, 0, 0);
		NewEmailTrigger newEmailTrigger = new NewEmailTrigger(emailtriggersetting);
		Response response = RestClient.doPost("JSON", albatrossURL, "email-triggers", albatrossToken + "123", null,
				true, newEmailTrigger);
		Assert.assertEquals("Request Failure", response.statusCode(), 401);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertTrue("Response body differs", jsonPath.get("error").toString().contains("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] getTriggerId() {

		List<Integer> hiringStageId = new ArrayList<>();
		JsonPath jsonPath1 = RestClient.doPost("JSON", albatrossURL,
				"global/get-hiring-pipeline-stages-for-usermode/get", albatrossToken, null, true, null).jsonPath();
		for (int i = 0; i < 3; i++) {
			hiringStageId.add(jsonPath1.get("data[" + i + "].id"));
		}
		Object data[][] = {
				{ 3, hiringStageId.get(1), fakerTrigger.getTriggerName(),
						"The selected emailtriggersetting.trigger is invalid." },
				{ 4, hiringStageId.get(2), null, "The emailtriggersetting.name field is required." } };
		return data;
	}

}
