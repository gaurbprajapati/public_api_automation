package io.recruitcrm.albatross.deal;

import java.util.HashMap;
import java.util.Map;


import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC2LevelAccessDataProvider;

import io.rcrm.api.javafaker.JavaFakerDeal;
import io.rcrm.api.pojo.albatross.deal.CreateDeal;

import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACDealCreateSecurityTest extends TestBase {

    private final JavaFakerDeal fakerDeal = new JavaFakerDeal();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;

    // Common deal data
    private String dealName = "Test Deal " + System.currentTimeMillis();

    // Constants for expected messages
    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Failed To Add Deal : Access Denied";

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }


    @DataProvider(name = "dealCreateAccessData")
    public Object[][] getDealAccessData(ITestContext context) {
        return RBAC2LevelAccessDataProvider.getDealAccessData(context);
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "dealCreateAccessData", groups = {"role-based", "deal-create-access"})
    public void createDealSecurityTest(String role, String access, int expectedStatusCode, String expectedMessage, String testDescription) {
        String token = albatrossTknMap.get(role);
        
        Response response = createDeal(role, token);
        validateDealResponse(response, expectedStatusCode, expectedMessage, testDescription);
    }

    private Response createDeal(String role, String token) {
        // Create deal using the Albatross API CreateDeal POJO
        CreateDeal.Deal deal = CreateDeal.Deal.builder()
                .name(dealName)
                .dealvalue(String.valueOf(fakerDeal.getDealValue()))
                .closedate(System.currentTimeMillis() / 1000 + 86400)
                .build();
        CreateDeal.SelectedOwner selectedOwner = CreateDeal.SelectedOwner.builder()
                .id(userIdsMap.get(role))
                .build();
        CreateDeal.SelectedDealType selectedDealType = CreateDeal.SelectedDealType.builder()
                .id(2) // New business
                .build();
        CreateDeal.SelectedDealStage selectedDealStage = CreateDeal.SelectedDealStage.builder()
                .id(1) // Open stage
                .percentage("100")
                .build();   
        
        CreateDeal createDeal = CreateDeal.builder()
                .deal(deal)
                .selectedOwner(selectedOwner)
                .selectedDealType(selectedDealType)
                .selectedDealStage(selectedDealStage)
                .build();
        
        return RestClient.doPost("JSON", albatrossURL, "deals", token, null, true, createDeal);
    }

    private void validateDealResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription 
                    + " - Expected status code: " + expectedStatusCode 
                    + " but got: " + response.getStatusCode(), e);
        }

        if (expectedStatusCode == 200) {
            if (SUCCESS_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("data.deal.slug", Matchers.notNullValue());
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription + " - " + e.getMessage(), e);
                }
            }
        } else if (expectedStatusCode == 401) {
            if (FORBIDDEN_MESSAGE.equals(expectedMessage)) {
                try {
                    response.then().body("message", Matchers.is(ACCESS_DENIED_MESSAGE));
                } catch (AssertionError e) {
                    throw new AssertionError("Test Case FAILED: " + testDescription 
                            + " - Expected '" + ACCESS_DENIED_MESSAGE + "' but got: " + response.jsonPath().getString("message"), e);
                }
            }
        }
    }
}
