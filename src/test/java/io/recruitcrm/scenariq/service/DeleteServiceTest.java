package io.recruitcrm.scenariq.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.RegisterServiceRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class DeleteServiceTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteServiceByOwnerTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Response response = deleteService(ownerToken, serviceId);

        assertThat("Owner deleting their own service should return 204",
                response.statusCode(), is(204));

        // Verify the service no longer exists
        Response getResponse = getServiceById(ownerToken, serviceId);
        assertThat("Fetching deleted service should return 404",
                getResponse.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteServiceByMemberWhoRegisteredTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        RegisterServiceRequest registerReq = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch("main")
                .automationBranch("main")
                .build();
        Response registerResp = registerService(memberToken, registerReq);
        assertThat("Prerequisite: Member service registration should return 201",
                registerResp.statusCode(), is(201));
        long serviceId = registerResp.jsonPath().getLong("id");

        Response response = deleteService(memberToken, serviceId);

        assertThat("Member who registered the service should be able to delete it and get 204",
                response.statusCode(), is(204));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteServiceByMemberWhoDidNotRegisterTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        long serviceId = createTestService(ownerToken);

        Object[] memberData = inviteAndAcceptUser(ownerToken, "MEMBER");
        String memberToken = (String) memberData[0];

        Response response = deleteService(memberToken, serviceId);

        assertThat("Member who did not register the service should get 403 when trying to delete it",
                response.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteServiceWithInvalidIdTest() {
        Object[] account = createScenariqAccount();
        String ownerToken = (String) account[0];

        Response response = deleteService(ownerToken, 999999999L);

        assertThat("Deleting a service with non-existent ID should return 404",
                response.statusCode(), is(404));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void deleteServiceWithInvalidTokenTest() {
        Response response = deleteService("invalid-token-xyz", 1L);

        assertThat("Deleting a service with invalid auth token should return 401",
                response.statusCode(), is(401));
    }
}
