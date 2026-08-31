package io.rcrm.api.company;

import java.io.IOException;
import java.util.*;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.CompanyInheritance;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class GetAllChildCompaniesTest extends TestBase {

	public GetAllChildCompaniesTest() {
		super();
	}

	commanFunction function = new commanFunction();
	commanFunction function1 = new commanFunction();
	String basePath = "companies/child-company-stats";

	@BeforeTest
	public void setUp() throws IOException {
		function = new commanFunction();
	}

	@Owner("Sai Teja SG")
	@Test(dataProvider = "getParentCompanyData", groups = "nightly-build")
	public void getAllChildCompanies_Test(String parentCompanySlug, String childCompanySlug) {

		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sortKey", "companyname");
		queryParameters.put("sortOrder", "asc");
		queryParameters.put("parent_company_slug", parentCompanySlug);

		Response response = RestClient.doGet("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 200);
		response.then().body("message", Matchers.is("Child company stats fetched successfully"));
		response.then().body("data[0].ParentSlug", Matchers.is(parentCompanySlug));
		response.then().body("data[0].CompanySlug", Matchers.is(childCompanySlug));
	}

	@Owner("Sai Teja SG")
	@Test(groups = "nightly-build")
	public void unauthorizedUserCannotGetAllChildCompanies_Test() {
		Map<String, String> queryParameters = new HashMap<String, String>();
		queryParameters.put("sortKey", "companyname");
		queryParameters.put("sortOrder", "asc");
		queryParameters.put("parent_company_slug", null);

		Response response = RestClient.doGet("JSON", albatrossURL, basePath,
				ThreadManager.getOwnerAlbatrossToken() + "x003", queryParameters, null, true);

		Assert.assertEquals(response.getStatusCode(), 401);
		response.then().body("error", Matchers.is("Unauthorized"));
	}

	@DataProvider
	public Object[][] getParentCompanyData() {

		JsonPath parentCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String parentCompanySlug = parentCompany.get("slug");

		JsonPath childCompany = function1.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey())
				.jsonPath();
		String childCompanySlug = childCompany.get("slug");
		List<String> childCompanies = new ArrayList<>();
		childCompanies.add(childCompanySlug);

		CompanyInheritance company = new CompanyInheritance();
		company.setChild_company_slugs(childCompanies);
		company.setParent_company_slug(parentCompanySlug);

		String basePath = "companies/link-to-parent-company";

		Response response = RestClient.doPost("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
				null, true, company);

		Assert.assertEquals(response.getStatusCode(), 200);

		Object data[][] = { { parentCompanySlug, childCompanySlug } };
		return data;
	}
}