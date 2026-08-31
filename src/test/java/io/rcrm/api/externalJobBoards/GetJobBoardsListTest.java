package io.rcrm.api.externalJobBoards;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetJobBoardsListTest extends TestBase {

	String albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Yash Rampal")
	@Test(groups = "nightly-build")
	public void getAllJobBoardsList_GET() {
		String basePath = "/jobboards/list";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, albatrossTkn, null, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("id", Matchers.notNullValue());
		response.then().body("account_id", Matchers.notNullValue());
		response.then().body("job_board_id", Matchers.notNullValue());
	}

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void unAuthorizedUserCannotAccessGetJobBoardslist() {
		String basePath = "/jobboards/list";

		Response response = RestClient.doGet("JSON", jobBoardServiceURL, basePath, albatrossTkn + "123", null, null,
				true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}