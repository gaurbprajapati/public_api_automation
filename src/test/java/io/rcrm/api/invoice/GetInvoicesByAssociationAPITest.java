package io.rcrm.api.invoice;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.*;

import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.pojo.UpdateInvoiceRequest;
import com.qa.api.util.Owner;
import org.json.JSONObject;

@AccountType("CrossAccount")
public class GetInvoicesByAssociationAPITest extends TestBase {

	String apiKeyA;
	String albatrossTknA;
	String albatrossTknB;
	commanFunction function;
	AllCrudFunctions allCrudFunctions;
	String basePath = "invoices/get-invoices-by-association";

	@BeforeClass(alwaysRun = true)
	public void setUp() {
		apiKeyA = getAccountApiKey("AccountA");
		albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknB = getTokenForAccount("AccountB", "valid");
		function = new commanFunction();
		allCrudFunctions = new AllCrudFunctions();
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getCandidateAndInvoiceData", groups = "nightly-build")
	public void getInvoicesByAssociationWithValidCandidate_Test(int invoiceId, int candidateId, String candidateSlug) {
		JSONObject body = buildPayload(5, candidateId);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getString("data.invoices[0].associations[0].candidates[0].slug"), is(candidateSlug));
		response.then().assertThat().body(matchesJsonSchemaInClasspath("publicApi//invoice//getInvoicesByAssociationAPI.json"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getInvoicesByAssociationWithNonExistentEntityId_Test() {
		JSONObject body = buildPayload(5, Integer.MAX_VALUE);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA, null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getInt("data.total"), is(0));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getCandidateAndInvoiceData", groups = "nightly-build")
	public void getInvoicesByAssociationWithCrossAccountToken_Test(int invoiceId, int candidateId, String candidateSlug) {
		JSONObject body = buildPayload(5, candidateId);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknB, null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getInt("data.total"), is(0));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getInvoicesByAssociationWithInvalidToken_Test() {
		JSONObject body = buildPayload(5, 999999999);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, albatrossTknA + "invalid", null, true, body);

		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("meta.message"), is("Unauthorised access"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getInvoicesByAssociationWithoutToken_Test() {
		JSONObject body = buildPayload(5, 999999999);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, "", null, true, body);

		assertThat(response.getStatusCode(), is(401));
		assertThat(response.jsonPath().getString("meta.message"), is("Unauthorised access"));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getCandidateAndInvoiceData", groups = "nightly-build")
	public void getInvoicesByAssociationWithAdminToken_Test(int invoiceId, int candidateId, String candidateSlug) {
		JSONObject body = buildPayload(5, candidateId);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getInt("data.total"), is(1));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getCandidateAndInvoiceData", groups = "nightly-build")
	public void getInvoicesByAssociationWithTeamMemberToken_Test(int invoiceId, int candidateId, String candidateSlug) {
		JSONObject body = buildPayload(5, candidateId);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getInt("data.total"), is(1));
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getCandidateAndInvoiceData", groups = "nightly-build")
	public void getInvoicesByAssociationWithRestrictedTeamMemberToken_Test(int invoiceId, int candidateId, String candidateSlug) {
		JSONObject body = buildPayload(5, candidateId);

		Response response = RestClient.doPost("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, body);

		assertThat(response.getStatusCode(), is(200));
		assertThat(response.jsonPath().getString("meta.message"), is("Invoices Fetched Successfully"));
		assertThat(response.jsonPath().getInt("data.total"), is(0));
	}

	@DataProvider(parallel = true)
	public Object[][] getCandidateAndInvoiceData() {
		String candidateSlug = function.createNewCandidateWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int candidateId = allCrudFunctions.getCandidateResponse(albatrossURL, albatrossTknA, candidateSlug).jsonPath().getInt("data.candidate.id");

		String companySlug = function.createNewCompanyWithMandatoryFields(baseURL, apiKeyA).jsonPath().getString("slug");
		int companyId = allCrudFunctions.getCompanyResponse(albatrossURL, albatrossTknA, companySlug).jsonPath().getInt("data.company.id");

		int invoiceId = function.createInvoice(invoiceServiceURL, albatrossTknA, companyId).jsonPath().get("data");

		Map<String, List<Integer>> associations = new HashMap<>();
		associations.put("2", new ArrayList<>());
		associations.put("3", new ArrayList<>());
		associations.put("4", new ArrayList<>());
		associations.put("5", Collections.singletonList(candidateId));
		associations.put("11", new ArrayList<>());

		UpdateInvoiceRequest patchBody = new UpdateInvoiceRequest();
		patchBody.setAssociations(associations);
		RestClient.doPatchOnce("JSON", invoiceServiceURL, "invoices/" + invoiceId, albatrossTknA, null, true, patchBody).then().statusCode(200);

		return new Object[][] { { invoiceId, candidateId, candidateSlug } };
	}

	private JSONObject buildPayload(int entityTypeId, int entityId) {
		JSONObject body = new JSONObject();
		body.put("entityTypeId", entityTypeId);
		body.put("entityId", entityId);
		body.put("page", 1);
		body.put("pageSize", 25);
		body.put("sortBy", "invoiceId");
		body.put("sortOrder", "desc");
		body.put("search", "");
		return body;
	}
}
