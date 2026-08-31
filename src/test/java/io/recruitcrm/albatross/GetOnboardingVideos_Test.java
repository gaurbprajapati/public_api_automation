package io.recruitcrm.albatross;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.testng.annotations.*;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import io.rcrm.api.pojo.albatross.GetOnboardingVideos;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetOnboardingVideos_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "get-onboarding-videos";
    GetOnboardingVideos getOnboardingVideos = new GetOnboardingVideos();

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

	@Owner("Smit Patel")
	@Test(dataProvider = "getOnboardingVideosData", groups = "nightly-build")
	public void getOnboardingVideos_Test(String page) {
		getOnboardingVideos.setPage(page);
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getOnboardingVideos);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosUnauthorized_Test() {
		getOnboardingVideos.setPage("Candidate List");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, true, getOnboardingVideos);
		assertThat(response.statusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosEmptyToken_Test() {
		getOnboardingVideos.setPage("Candidate List");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, "", null, true, getOnboardingVideos);
		assertThat(response.statusCode(), is(401));
		assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosInvalidPage_Test() {
		getOnboardingVideos.setPage("Invalid Page");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getOnboardingVideos);
		assertThat(response.statusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is("Videos not available"));
		assertThat(response.jsonPath().getString("message_type"), is("is-danger"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosEmptyPage_Test() {
		getOnboardingVideos.setPage("");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getOnboardingVideos);
		assertThat(response.statusCode(), is(200));
		assertThat(response.jsonPath().getString("message"), is(""));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosCrossAccount_Test() {
		getOnboardingVideos.setPage("Candidate List");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, true, getOnboardingVideos);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosAdminToken_Test() {
		getOnboardingVideos.setPage("Candidate List");
		String adminToken = getRoleBasedToken("AccountA", "Admin");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, adminToken, null, true, getOnboardingVideos);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosTeamMemberToken_Test() {
		getOnboardingVideos.setPage("Candidate List");
		String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, teamMemberToken, null, true, getOnboardingVideos);
		validateSuccessResponse(response);
	}

	@Owner("Smit Patel")
	@Test(groups = "nightly-build")
	public void getOnboardingVideosRestrictedToken_Test() {
		getOnboardingVideos.setPage("Candidate List");
		String restrictedToken = getRoleBasedToken("AccountA", "Restricted");
		Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedToken, null, true, getOnboardingVideos);
		validateSuccessResponse(response);
	}

	private void validateSuccessResponse(Response response) {
		assertThat(response.statusCode(), is(200));
		assertThat(response.jsonPath().getString("message_type"), is("is-success"));
		assertThat(response.jsonPath().getString("status"), is("success"));
		assertThat(response.jsonPath().getInt("data.videos.size()"), is(greaterThan(0)));
	}

	@DataProvider
	public Object[][] getOnboardingVideosData() {
		return new Object[][] {
			{ "Candidate List" },
			{ "Job List" },
			{ "Company List" },
			{ "Contact List" }
		};
	}
}
