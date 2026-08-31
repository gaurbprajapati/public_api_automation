package io.recruitcrm.albatross.offlimit;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus;
import io.rcrm.api.pojo.albatross.offlimit.OffLimitStatus.offLimitStatus;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class OffLimitStatusCurdTest extends TestBase {

	commanFunction function = new commanFunction();
	AllCrudFunctions albatrossFunctions = new AllCrudFunctions();
	String generatedString = RandomStringUtils.randomAlphabetic(4);
	int status_id;
	int candidateId = -1;
	int companyId = -1;
	int contactId = -1;
	
	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void addOffLimitStatusWithInvalidAuth() {
		offLimitStatus offLimitStatus = new offLimitStatus();
		offLimitStatus.setStatus_label("Off Limit " + generatedString);
		offLimitStatus.setStatus_colour_id("A1");
		offLimitStatus.setSequence_no(1);
		offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
		offLimitStatus.setDefaultStatus("0");
		offLimitStatus.setOfflimit_status_colour_id("A1");
		offLimitStatus.setBackground_color_hex("#FEE2E1");
		offLimitStatus.setText_color_hex("#9E4D4D");
		offLimitStatus.setCount(0);

		OffLimitStatus offLimitStatusBody = new OffLimitStatus();
		offLimitStatusBody.setOffLimitStatus(new offLimitStatus[] {offLimitStatus});

		String basePath = "off-limit/status";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true, offLimitStatusBody);

		response.then().statusCode(401);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOffLimitStatusWithInvalidAuth() {
		String basePath = "off-limit/status";

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true);

		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}

	@Owner("Akshaya Uppala")
	@Test(groups = "nightly-build")
	public void updateOffLimitStatusWithInvalidAuth() {
		offLimitStatus offLimitStatus = new offLimitStatus();
		offLimitStatus.setStatus_label("Off Limit " + generatedString + " Updated");
		offLimitStatus.setStatus_colour_id("A1");
		offLimitStatus.setSequence_no(1);
		offLimitStatus.setAccount_id(String.valueOf(ThreadManager.getAccount().getAccountId()));
		offLimitStatus.setDefaultStatus("0");
		offLimitStatus.setOfflimit_status_colour_id("B1");
		offLimitStatus.setBackground_color_hex("#FFF7ED");
		offLimitStatus.setText_color_hex("#8F6A3C");
		offLimitStatus.setCount(0);

		OffLimitStatus offLimitStatusBody = new OffLimitStatus();
		offLimitStatusBody.setOffLimitStatus(new offLimitStatus[] {offLimitStatus});

		String basePath = "off-limit/status";

		Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"123", null, null, true, offLimitStatusBody);

		response.then().statusCode(401);
		Assert.assertEquals(response.jsonPath().get("error"), "Unauthorized");
	}


}
