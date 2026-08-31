package io.recruitcrm.albatross.xmlfeed;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.javafaker.JavaFakerXmlFeed;
import io.rcrm.api.pojo.albatross.xmlfeed.SaveCustomXmlFeed;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AllEndpointsXmlFeedJobBoardServiceTest extends TestBase {

	JavaFakerXmlFeed javaFakerXmlFeed = new JavaFakerXmlFeed();
	String xmlHeader = javaFakerXmlFeed.getXmlHeader();
	String xmlBody = javaFakerXmlFeed.getXmlBody();
	String albatrossTkn;

	@BeforeClass(alwaysRun = true)	public void setUp() throws IOException {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void createNewCustomXmlFeed() {
		String xmlFeedTitle = javaFakerXmlFeed.getXmlFeedTitle();
		
		SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(xmlFeedTitle, xmlHeader, xmlBody, 0, 0, 30);
		String basePath = "custom-xml/save";
		
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, albatrossTkn, null, true,
				saveCustomXmlFeed);
		Assert.assertEquals(response.getStatusCode(), 200);
		
		response.then().body("message", Matchers.is("XML Feed Added"));
		response.then().body("data.title", Matchers.is(xmlFeedTitle));
		response.then().body("data.parent_xml", Matchers.is(xmlHeader));
		response.then().body("data.dynamic_job_xml", Matchers.is(xmlBody));
		response.then().body("data.decode_xml_data", Matchers.is(0));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//xmlFeed//SaveCustomXmlFeed.json"));
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "saveCustomXmlFeed", groups = "nightly-build")
	public void editCustomXmlFeed(int xmlFeedId) {
		String xmlFeedTitle = javaFakerXmlFeed.getXmlFeedTitle();
		
		SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(xmlFeedTitle, xmlHeader, xmlBody, 1, 1, 30);
		String basePath = "custom-xml/update-feed/" + xmlFeedId;
		
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, basePath, albatrossTkn, null, true,
				saveCustomXmlFeed);
		Assert.assertEquals(response.getStatusCode(), 200);
		
        response.then().body("message", Matchers.is("XML Feed Updated"));
		response.then().body("data.title", Matchers.is(xmlFeedTitle));
		response.then().body("data.parent_xml", Matchers.is(xmlHeader));
		response.then().body("data.dynamic_job_xml", Matchers.is(xmlBody));
		response.then().body("data.decode_xml_data", Matchers.is(1));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi//xmlFeed//SaveCustomXmlFeed.json"));
	}

	@DataProvider(parallel = true)
	public Object[][] saveCustomXmlFeed() {

		SaveCustomXmlFeed saveCustomXmlFeed = new SaveCustomXmlFeed(javaFakerXmlFeed.getXmlFeedTitle(), xmlHeader,
				xmlBody, 0, 0, 30);
		Response response = RestClient.doPost("JSON", jobBoardServiceURL, "custom-xml/save", albatrossTkn, null, true,
				saveCustomXmlFeed);
		Assert.assertEquals(response.getStatusCode(), 200);

		int xmlFeedId = response.jsonPath().getInt("data.id");

		return new Object[][] { { xmlFeedId } };
	}

}
