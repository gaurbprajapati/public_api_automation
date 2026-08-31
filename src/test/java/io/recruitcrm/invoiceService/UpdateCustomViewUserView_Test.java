package io.recruitcrm.invoiceService;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import java.util.*;

import org.hamcrest.Matchers;
import org.testng.annotations.*;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.rcrm.api.javafaker.*;
import io.rcrm.api.pojo.invoiceService.UpdateUserViewRequest;
import com.qa.api.util.Owner;

@AccountType("CrossAccount")
public class UpdateCustomViewUserView_Test extends TestBase {

    String albatrossTknA;
    String albatrossTknB;
    JavaFakerPlacement placementFaker;
    String basePath = "custom-view/user-view";

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossTknA = getTokenForAccount("AccountA", "valid");
        albatrossTknB = getTokenForAccount("AccountB", "valid");
        placementFaker = new JavaFakerPlacement();
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithValidToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
        response.then().assertThat().body(matchesJsonSchemaInClasspath("privateApi/invoiceService/updateUserView.json"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithInvalidToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, placementFaker.getInvalidToken(), null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithoutToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, "", null, null, true, requestBody);
        response.then().statusCode(401);
        response.then().assertThat().body("meta.message", Matchers.is("Unauthorised access"));
        response.then().assertThat().body("data", Matchers.is("Missing bearer token in header"));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithCrossAccountToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknB, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithAdminToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Admin"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithTeamMemberToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Team Member"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "updateEntityIdData", groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithRestrictedTeamMemberToken_Test(int entityId, List<Integer> listActions) {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(entityId);
        requestBody.setListActions(listActions);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, getRoleBasedToken("AccountA", "Restricted"), null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
        response.then().assertThat().body("data", Matchers.nullValue());
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithInvalidEntityId_Test() {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(999999);
        requestBody.setListActions(Arrays.asList(1, 2, 3, 4, 8, 5, 6, 7));

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(200);
        response.then().assertThat().body("meta.message", Matchers.is("Updated User View Successfully."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithNullEntityId_Test() {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(null);
        requestBody.setListActions(Arrays.asList(1, 2, 3, 4, 8, 5, 6, 7));

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(Matchers.is(400));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithEmptyListActions_Test() {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(15);
        requestBody.setListActions(new ArrayList<>());

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(400);
        response.then().assertThat().body("errors[0].message", Matchers.is("Field listActions cannot be empty."));
    }

    @Owner("Smit Patel")
    @Test(groups = {"invoice_service", "nightly-build"})
    public void updateCustomViewUserViewWithNullListActions_Test() {
        UpdateUserViewRequest requestBody = new UpdateUserViewRequest();
        requestBody.setEntityId(15);
        requestBody.setListActions(null);

        Response response = RestClient.doPut1("JSON", invoiceServiceURL, basePath, albatrossTknA, null, null, true, requestBody);
        response.then().statusCode(400);
        response.then().assertThat().body("errors[0].message", Matchers.is("Field listActions cannot be empty."));
    }

    @DataProvider(parallel = true)
    public Object[][] updateEntityIdData() {
        return new Object[][] {
            { 15, Arrays.asList(1, 2, 3, 4, 8, 5, 6, 7) }
        };
    }
}
