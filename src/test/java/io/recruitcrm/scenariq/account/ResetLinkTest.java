package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class ResetLinkTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void generateResetLinkByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) member[1];

        Response response = generateResetLink(ownerToken, memberUserId);

        assertThat("Owner generating reset link for member should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Reset link should be present in the response",
                jsonPath.getString("resetLink"), notNullValue());
        assertThat("Reset link should not be empty",
                jsonPath.getString("resetLink"), not(emptyOrNullString()));
        assertThat("Expiry time should be present in the response",
                jsonPath.getString("expiryTime"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void generateResetLinkByNonOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];
        long ownerUserId = (long) account[3];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) member[0];

        Response response = generateResetLink(memberToken, ownerUserId);

        assertThat("Non-owner generating reset link should return 403",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void generateResetLinkForNonExistentUserTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Response response = generateResetLink(ownerToken, 999999999L);

        assertThat("Generating reset link for non-existent user should return 404",
                response.statusCode(), is(404));
    }
}
