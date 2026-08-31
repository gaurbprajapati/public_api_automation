package io.rcrm.api.list;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetXmlFeedListTest extends TestBase {

    AllCrudFunctions allCrudFunctions = new AllCrudFunctions();

    int[] defaultFeedIds;
    String[] defaultFeedNames;
    int[] customFeedIds;
    String[] customFeedNames;

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
	public void getDefaultXmlFeedList() {
    	Integer[] defaultFeedIds = { 1, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 18, 19 };
		String[] defaultFeedNames = { "Free Job Boards", "Indeed", "JobIsJob", "JobAdX", "Post Job Free", "JobInventory", "Adzuna", "Talroo", "MyJobHelper", "Joblift", "Remotive", "Organic Job Boards", "Twine" };
		String basePath = "jobs/list-xml-jobboards";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null, true);
		
		Assert.assertEquals(response.getStatusCode(), 200);
		for (int i = 0; i < response.jsonPath().getList("default_xml_feeds").size(); i++) {
			response.then().body("default_xml_feeds[" + i + "].id", Matchers.is(defaultFeedIds[i]));
			response.then().body("default_xml_feeds[" + i + "].label", Matchers.is(defaultFeedNames[i]));
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//list//getXmlFeedList.json"));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void getCustomXmlFeedList() {

		JsonPath jsonFeed = allCrudFunctions
				.createCustomXmlFeed(jobBoardServiceURL, ThreadManager.getOwnerAlbatrossToken()).jsonPath();
		int[] customFeedIds = new int[] { jsonFeed.getInt("data.id") };
		String[] customFeedNames = new String[] { jsonFeed.getString("data.title") };
		String basePath = "jobs/list-xml-jobboards";
		Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey(), null, null,
				true);
		Assert.assertEquals(response.getStatusCode(), 200);

		for (int i = 0; i < response.jsonPath().getList("custom_xml_feeds").size(); i++) {
			response.then().body("custom_xml_feeds[" + i + "].id", Matchers.is(customFeedIds[i]));
			response.then().body("custom_xml_feeds[" + i + "].label", Matchers.is(customFeedNames[i]));
		}
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//list//getXmlFeedList.json"));
	}


    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getXmlFeedListWithInvalidAuth() {
        String basePath = "jobs/list-xml-jobboards";
        Response response = RestClient.doGet("JSON", baseURL, basePath, ThreadManager.getAccountApiKey()+"123", null, null, true);
        assert response != null : "Response is null";
        response.then().statusCode(401);
        response.then().body("error", Matchers.is("Unauthorized"));
    }

}
