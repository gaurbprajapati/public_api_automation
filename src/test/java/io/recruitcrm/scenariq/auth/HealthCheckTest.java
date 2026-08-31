package io.recruitcrm.scenariq.auth;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class HealthCheckTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void healthCheckTest() {
        Response response = healthCheck();

        assertThat("Health check endpoint should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Health check status should be 'up'",
                jsonPath.getString("status"), equalToIgnoringCase("up"));
    }
}
