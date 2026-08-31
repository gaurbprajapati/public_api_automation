package io.recruitcrm.premiumJobBoard;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.premiumJobBoard.GetCampaignDraftList;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllDraftVonqTest extends TestBase {

	public GetAllDraftVonqTest() {
		super();
	}


	@Owner("Sampurn Chouksey")
	@Test(groups = "nightly-build")
	public void getAllDraftCampaignWithInvalidBody() {
		GetCampaignDraftList getCampaignDraftList = new GetCampaignDraftList();
		getCampaignDraftList.setSort_by("");
		getCampaignDraftList.setSort_order("");
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft-list",
				ThreadManager.getOwnerAlbatrossToken(), null, false, getCampaignDraftList);

		response.then().statusCode(422);
		response.then().body("message", Matchers.is("The given data was invalid."));
		response.then().body("errors.sort_by[0]", Matchers.is("The sort by field is required."));
		response.then().body("errors.sort_order[0]", Matchers.is("The sort order field is required."));
	}

	@Owner("Gaurav Prajapati")
	@Test(groups = "nightly-build")
	public void getAllDraftCampaignWithInvalidAuth() {
		GetCampaignDraftList getCampaignDraftList = new GetCampaignDraftList();
		getCampaignDraftList.setSort_by("created_on");
		getCampaignDraftList.setSort_order("desc");
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "vonq/campaigns/draft-list",
				ThreadManager.getOwnerAlbatrossToken() + 123, null, false, getCampaignDraftList);

		response.then().statusCode(401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}
}
