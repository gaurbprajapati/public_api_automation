package io.recruitcrm.scenariq.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.RegisterServiceRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class RegisterServiceTest extends ScenariqBaseTest {

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithValidDataTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        String serviceName = scenariqFaker.getServiceName();
        String serviceType = "SPRING_BOOT";
        String backendRepoUrl = scenariqFaker.getBackendRepoUrl();
        String automationRepoUrl = scenariqFaker.getAutomationRepoUrl();

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(serviceName)
                .serviceType(serviceType)
                .backendRepoUrl(backendRepoUrl)
                .automationRepoUrl(automationRepoUrl)
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with valid data should return 201",
                response.statusCode(), is(201));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Service id should be present in register response",
                jsonPath.get("id"), notNullValue());
        assertThat("Service name should match the name sent in request",
                jsonPath.getString("serviceName"), is(serviceName));
        assertThat("Service type should match the type sent in request",
                jsonPath.getString("serviceType"), is(serviceType));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithAllFieldsTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        String serviceName = scenariqFaker.getServiceName();
        String ownerName = scenariqFaker.getOwnerName();
        String backendBranch = scenariqFaker.getBranchName();
        String automationBranch = scenariqFaker.getBranchName();
        String bindingKeyword = scenariqFaker.getBindingKeyword();
        String gitUsername = scenariqFaker.getGitUsername();
        String gitToken = scenariqFaker.getGitToken();

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(serviceName)
                .ownerName(ownerName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .backendBranch(backendBranch)
                .automationBranch(automationBranch)
                .backendControllersPath("src/main/java/com/example/controllers")
                .automationTestsPath("src/test/java/com/example/tests")
                .bindingKeyword(bindingKeyword)
                .gitUsername(gitUsername)
                .gitToken(gitToken)
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with all fields should return 201",
                response.statusCode(), is(201));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Service id should be present in register response",
                jsonPath.get("id"), notNullValue());
        assertThat("Service name should match the name sent in request",
                jsonPath.getString("serviceName"), is(serviceName));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithMissingNameTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with missing name should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithMissingTypeTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with missing type should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithInvalidTypeTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("INVALID_TYPE")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with invalid type should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithMissingBackendRepoTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with missing backend repo URL should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithMissingAutomationRepoTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .build();

        Response response = registerService(token, request);

        assertThat("Register service with missing automation repo URL should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithDuplicateNameTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        String duplicateName = scenariqFaker.getServiceName();

        RegisterServiceRequest firstRequest = RegisterServiceRequest.builder()
                .serviceName(duplicateName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response firstResponse = registerService(token, firstRequest);
        assertThat("Prerequisite: first service registration should return 201",
                firstResponse.statusCode(), is(201));

        RegisterServiceRequest secondRequest = RegisterServiceRequest.builder()
                .serviceName(duplicateName)
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response secondResponse = registerService(token, secondRequest);

        assertThat("Register service with duplicate name should return 400 or 409",
                secondResponse.statusCode(), anyOf(is(400), is(409)));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceExceedingPlanLimitTest() {
        Object[] account = createScenariqAccount();
        String token = (String) account[0];

        RegisterServiceRequest firstRequest = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response firstResponse = registerService(token, firstRequest);
        assertThat("Prerequisite: first service registration on FREE plan should return 201",
                firstResponse.statusCode(), is(201));

        RegisterServiceRequest secondRequest = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response secondResponse = registerService(token, secondRequest);

        assertThat("Register second service on FREE plan should return 403",
                secondResponse.statusCode(), is(403));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void registerServiceWithInvalidTokenTest() {
        RegisterServiceRequest request = RegisterServiceRequest.builder()
                .serviceName(scenariqFaker.getServiceName())
                .serviceType("SPRING_BOOT")
                .backendRepoUrl(scenariqFaker.getBackendRepoUrl())
                .automationRepoUrl(scenariqFaker.getAutomationRepoUrl())
                .build();

        Response response = registerService("invalid-token-xyz", request);

        assertThat("Register service with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
