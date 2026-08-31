package io.recruitcrm.scenariq.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.*;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class UpdateAccountUserTest extends ScenariqBaseTest {

    // -- PUT /accounts/users/{id} -- owner updates member role to ADMIN --------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateUserRoleByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) member[1];

        UpdateAccountUserRequest request = UpdateAccountUserRequest.builder()
                .role("ADMIN")
                .build();

        Response response = updateAccountUser(ownerToken, memberUserId, request);

        assertThat("Owner updating member role to ADMIN should return 200",
                response.statusCode(), is(200));

        Response usersResponse = getAccountUsers(ownerToken);
        JsonPath jsonPath = usersResponse.jsonPath();
        String updatedRole = jsonPath.getList("findAll { it.id == " + memberUserId + " }.role").get(0).toString();
        assertThat("Member role should be updated to ADMIN",
                updatedRole, is("ADMIN"));
    }

    // -- PUT /accounts/users/{id} -- member tries to update owner role ---------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateUserRoleByNonOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];
        long ownerUserId = (long) account[3];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) member[0];

        UpdateAccountUserRequest request = UpdateAccountUserRequest.builder()
                .role("MEMBER")
                .build();

        Response response = updateAccountUser(memberToken, ownerUserId, request);

        assertThat("Non-owner updating another user's role should return 403",
                response.statusCode(), is(403));
    }

    // -- PUT /accounts/users/{id} -- owner updates member status to INACTIVE ---

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateUserStatusByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) member[1];

        UpdateAccountUserRequest request = UpdateAccountUserRequest.builder()
                .status("INACTIVE")
                .build();

        Response response = updateAccountUser(ownerToken, memberUserId, request);

        assertThat("Owner updating member status to INACTIVE should return 200",
                response.statusCode(), is(200));

        Response usersResponse = getAccountUsers(ownerToken);
        JsonPath jsonPath = usersResponse.jsonPath();
        String updatedStatus = jsonPath.getList("findAll { it.id == " + memberUserId + " }.status").get(0).toString();
        assertThat("Member status should be updated to INACTIVE",
                updatedStatus, is("INACTIVE"));
    }

    // -- PUT /accounts/users/{id} -- update non-existent user ------------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void updateNonExistentUserTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        UpdateAccountUserRequest request = UpdateAccountUserRequest.builder()
                .role("ADMIN")
                .build();

        Response response = updateAccountUser(ownerToken, 999999999L, request);

        assertThat("Updating a non-existent user should return 404",
                response.statusCode(), is(404));
    }

    // -- PUT /accounts/users/{id} -- disable then re-enable member -------------

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void disableAndReEnableUserTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] member = inviteAndAcceptUser(ownerToken, "MEMBER");
        long memberUserId = (long) member[1];

        UpdateAccountUserRequest disableRequest = UpdateAccountUserRequest.builder()
                .status("INACTIVE")
                .build();
        Response disableResponse = updateAccountUser(ownerToken, memberUserId, disableRequest);

        assertThat("Disabling member should return 200",
                disableResponse.statusCode(), is(200));

        UpdateAccountUserRequest enableRequest = UpdateAccountUserRequest.builder()
                .status("ACTIVE")
                .build();
        Response enableResponse = updateAccountUser(ownerToken, memberUserId, enableRequest);

        assertThat("Re-enabling member should return 200",
                enableResponse.statusCode(), is(200));

        Response usersResponse = getAccountUsers(ownerToken);
        JsonPath jsonPath = usersResponse.jsonPath();
        String finalStatus = jsonPath.getList("findAll { it.id == " + memberUserId + " }.status").get(0).toString();
        assertThat("Member status should be ACTIVE after re-enabling",
                finalStatus, is("ACTIVE"));
    }
}
