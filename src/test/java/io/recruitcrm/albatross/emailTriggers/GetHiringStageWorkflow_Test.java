package io.recruitcrm.albatross.emailTriggers;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerTrigger;
import io.rcrm.api.pojo.albatross.emailtrigger.Emailtriggersetting;
import io.rcrm.api.pojo.albatross.emailtrigger.GetEmailTriggerList;
import io.rcrm.api.pojo.albatross.emailtrigger.NewEmailTrigger;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetHiringStageWorkflow_Test extends TestBase {

	String albatrossToken = "";

	@BeforeClass(alwaysRun = true)	public void getAccountAPI() {
		albatrossToken = ThreadManager.getOwnerAlbatrossToken();
	}

	JavaFakerTrigger fakerTrigger = new JavaFakerTrigger();

	@Owner("Divya")
	@Test(dataProvider = "getTriggerId", groups = "nightly-build")
	public void getHiringStageWorkflowWithTriggerTypeIdTest_200(int trigger) {

		Map<String, String> queryParamters = new HashMap<String, String>();
		queryParamters.put("triggerType", String.valueOf(trigger));
		Response response = RestClient.doGet("JSON", albatrossURL,
				"email-triggers/get-hiring-stage-workflow-email-triggers", albatrossToken, queryParamters, null, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);
		response.then().body("status", Matchers.containsString("success"));
		response.then().body("message_type", Matchers.containsString("is-success"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("getHiringStageWorkflowWithTriggetTypeId.json"));

	}

	@Owner("Divya")
	@Test(groups = "nightly-build")
	public void getUnauthorizedAccessHiringStageWorkflowTest_401() {

		Response response = RestClient.doGet("JSON", albatrossURL,
				"email-triggers/get-hiring-stage-workflow-email-triggers", albatrossToken + "123", null, null, true);
		Assert.assertEquals("Request Failure", response.statusCode(), 401);
		JsonPath jsonPath = response.jsonPath();
		Assert.assertTrue("Response body differs", jsonPath.get("error").toString().contains("Unauthorized"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("unauthorizedAccess.json"));

	}

	@DataProvider(parallel = true)
	public Object[][] getTriggerId() {

		List<Integer> hiringStageIds = new ArrayList<>();
		JsonPath jsonPath1 = RestClient.doPost("JSON", albatrossURL,
				"global/get-hiring-pipeline-stages-for-usermode/get", albatrossToken, null, true, null).jsonPath();
		for (int i = 0; i < 3; i++) {
			hiringStageIds.add(jsonPath1.get("data[" + i + "].id"));
		}

		int hiringStageId = hiringStageIds.get(fakerTrigger.getRandomIntValue(0, 2));
		Emailtriggersetting emailtriggersetting = new Emailtriggersetting(fakerTrigger.getTriggerName(), 3,
				hiringStageId, 0);
		NewEmailTrigger newEmailTrigger = new NewEmailTrigger(emailtriggersetting);

		Response response = RestClient.doPost("JSON", albatrossURL, "email-triggers", albatrossToken, null, true,
				newEmailTrigger);
		Assert.assertEquals("Request Failure", response.statusCode(), 200);

		GetEmailTriggerList getEmailTriggerList = new GetEmailTriggerList();
		getEmailTriggerList.setPage_size(fakerTrigger.getRandomIntValue(2, 20));

		JsonPath jsonPath = RestClient
				.doPost("JSON", albatrossURL, "email-triggers/get", albatrossToken, null, true, getEmailTriggerList)
				.jsonPath();
		int triggerId = jsonPath.get("data.records[0].trigger");

		Object[][] data = { { triggerId } };
		return data;
	}
}
