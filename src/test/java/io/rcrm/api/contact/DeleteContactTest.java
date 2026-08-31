package io.rcrm.api.contact;

import java.util.*;

import com.qa.api.util.reaper.ThreadManager;
import org.hamcrest.Matchers;
import org.testng.annotations.*;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import com.qa.api.util.Owner;

@AccountType("Business")
public class DeleteContactTest extends TestBase {

	public DeleteContactTest() {
		super();
	}

	JavaFakerContact faker = new JavaFakerContact();
	commanFunction function = new commanFunction();
	String apiAuthToken;

	@BeforeClass(alwaysRun = true)	public void setUp() {
		apiAuthToken = ThreadManager.getAccountApiKey();
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getContactData", groups = "nightly-build")
	public void deleteContactBySlug_Test(String contactSlug) {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", contactSlug);

		String basePath = "contacts/{contact}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);

		response.then().statusCode(200);
		response.then().body(Matchers.containsString("Deleted Successfully!"));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//contact//deleteContact.json"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void deleteContactByInvalidSlug_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", faker.getInvalidContactSlug());

		String basePath = "contacts/{contact}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken, null, pathParamters, true);

		response.then().statusCode(404);
		response.then().body("errorMessage", Matchers.is("Contact doesn't exist"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotDeleteContact_Test() {
		Map<String, String> pathParamters = new HashMap<String, String>();
		pathParamters.put("contact", faker.getInvalidContactSlug());
		String basePath = "contacts/{contact}";

		Response response = RestClient.doDelete("JSON", baseURL, basePath, apiAuthToken + "123", null, pathParamters,
				true);

		response.then().statusCode(401);
		response.then().body("error", Matchers.containsString("Unauthorized"));
	}

	@DataProvider(parallel = true)
	public Object[][] getContactData() {
		JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
		String companySlug = jsonCompany.get("slug");
		JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
		String contactSlug = jsonContact.get("slug");
		Object data[][] = { { contactSlug } };
		return data;
	}

}
