package io.recruitcrm.albatross;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import org.json.JSONObject;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetJobStatusByAccount_Test extends TestBase {

    private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
    commanFunction function = new commanFunction();
	String basePath = "jobs/job-status-by-account/get";

    @BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccount_Test() {
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, null);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccountUnauthorized_Test() {
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, true, null);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), containsString("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccountEmptyToken_Test() {
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, "", null, true, null);
        assertThat(response.getStatusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), containsString("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "getJobStatusByAccountData", groups = "nightly-build")
    public void getJobStatusByAccountCrossAccount_Test(String jobStatusLabel) {
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, true, null);
        validateSuccessResponse(response);
        List<String> labels = response.jsonPath().getList("data.customizedJobStatus.label");
        assertThat(labels, not(hasItem(jobStatusLabel)));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccountAdminToken_Test() {
        String adminToken = getRoleBasedToken("AccountA", "Admin");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, adminToken, null, true, null);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccountTeamMemberToken_Test() {
        String teamMemberToken = getRoleBasedToken("AccountA", "Team Member");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, teamMemberToken, null, true, null);
        validateSuccessResponse(response);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getJobStatusByAccountRestrictedTeamMemberToken_Test() {
        String restrictedTeamMemberToken = getRoleBasedToken("AccountA", "Restricted");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, restrictedTeamMemberToken, null, true, null);
        validateSuccessResponse(response);
    }

    private void validateSuccessResponse(Response response) {
        assertThat(response.getStatusCode(), is(200));
        assertThat(response.jsonPath().getString("message_type"), containsString("is-success"));
        assertThat(response.jsonPath().getString("status"), containsString("success"));
    }

    @DataProvider
    public Object[][] getJobStatusByAccountData() {
        Map<String, Integer> jobStatusValues = function.getJobStatusValues(albatrossURL, albatrossTknA);
        List<JSONObject> customizedJobStatusList = new ArrayList<>();
        customizedJobStatusList.add(new JSONObject()
            .put("label", jobStatusValues.keySet().iterator().next())
            .put("id", jobStatusValues.values().iterator().next())
            .put("accountid", 0)
            .put("sequenceno", 1));
        customizedJobStatusList.add(new JSONObject()
            .put("label", "Test Job Status"));

        Response response = function.createCustomJobStatus(albatrossURL, albatrossTknA, customizedJobStatusList);
        validateSuccessResponse(response);
        return new Object[][] {
            { response.jsonPath().getString("data.customizedJobStatus.label") }
        };
    }
}
