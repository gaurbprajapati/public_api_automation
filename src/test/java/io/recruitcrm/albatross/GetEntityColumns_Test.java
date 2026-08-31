package io.recruitcrm.albatross;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.testng.annotations.*;

import io.rcrm.api.pojo.albatross.GetEntityColumns;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import java.util.*;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class GetEntityColumns_Test extends TestBase {

	private String albatrossTknA;
	private String albatrossTknInvalidA;
	private String albatrossTknB;
	String basePath = "global/get-entity-columns";
    GetEntityColumns getEntityColumns = new GetEntityColumns();

	@BeforeClass(alwaysRun = true)	public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
		albatrossTknInvalidA = getTokenForAccount("AccountA", "invalid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
	}

    @Owner("Smit Patel")
    @Test(dataProvider = "getEntityColumnsData", groups = "nightly-build")
    public void getEntityColumns_Test(String entity) {
        HashMap<String, String> entityMap = new HashMap<>();
        entityMap.put("candidates", "candidate");
        entityMap.put("jobs", "job");
        entityMap.put("contacts", "contact");
        entityMap.put("reports_pitch_candidates", "candidate");
        entityMap.put("reports_assigned_candidates", "assignjobcandidate");
        entityMap.put("assigned_candidates", "assignjobcandidate");
        entityMap.put("tasks", "tasks");
        
        getEntityColumns.setEntity(entity);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getEntityColumns);
        String expectedEntity = entityMap.getOrDefault(entity, entity);
        validateSuccessResponse(response, expectedEntity);
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsUnauthorized_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknInvalidA, null, true, getEntityColumns);
        assertThat(response.statusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsEmptyToken_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, "", null, true, getEntityColumns);
        assertThat(response.statusCode(), is(401));
        assertThat(response.jsonPath().getString("error"), is("Unauthorized"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsInvalidEntity_Test() {
        getEntityColumns.setEntity("invalid_entity");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getEntityColumns);
        assertThat(response.statusCode(), is(500));
        assertThat(response.jsonPath().getString("exception"), is("ErrorException"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsEmptyEntity_Test() {
        getEntityColumns.setEntity("");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknA, null, true, getEntityColumns);
        assertThat(response.statusCode(), is(500));
        assertThat(response.jsonPath().getString("exception"), is("ErrorException"));
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsCrossAccount_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, albatrossTknB, null, true, getEntityColumns);
        validateSuccessResponse(response, "candidate");
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsAdmin_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, true, getEntityColumns);
        validateSuccessResponse(response, "candidate");
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsTeamMember_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, true, getEntityColumns);
        validateSuccessResponse(response, "candidate");
    }

    @Owner("Smit Patel")
    @Test(groups = "nightly-build")
    public void getEntityColumnsRestricted_Test() {
        getEntityColumns.setEntity("candidates");
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, true, getEntityColumns);
        validateSuccessResponse(response, "candidate");
    }

    private void validateSuccessResponse(Response response, String entity) {
        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("message_type"), is("is_success"));
        assertThat(response.jsonPath().getString("status"), is("success"));
        assertThat(response.jsonPath().getString("data.columns.id.entity"), is(entity));
    }

    @DataProvider
    public Object[][] getEntityColumnsData() {
        return new Object[][] {
            { "candidates" },
            { "contacts" },
            { "jobs" },
            { "reports_pitch_candidates" },
            { "reports_assigned_candidates" },
            { "assigned_candidates" },
            { "tasks" }
        };
    }
}
