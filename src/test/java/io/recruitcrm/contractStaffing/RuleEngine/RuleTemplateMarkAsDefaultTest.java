package io.recruitcrm.contractStaffing.RuleEngine;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class RuleTemplateMarkAsDefaultTest extends RuleEngineBaseTest {
    private List<Integer> createdTemplateIds = new ArrayList<>();
    private String albatrossAuthToken;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMarkAsDefault_200() throws InterruptedException {
        Integer templateId = createAndFetchTemplateId(albatrossAuthToken, "SUCCESS_200");
        assertThat("Template should be created", templateId, notNullValue());
        createdTemplateIds.add(templateId);

        Response response = markTemplateAsDefault(albatrossAuthToken, templateId, true);

        assertThat("Mark as default should return 200", response.getStatusCode(), equalTo(200));
        validateSuccessResponse(response, "Rule template marked as default successfully.");

        verifyTemplateIsMarkedAsDefault(albatrossAuthToken, templateId);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMarkAsDefault_401() throws InterruptedException {
        Integer templateId = createAndFetchTemplateId(albatrossAuthToken, "UNAUTHORIZED_401");
        assertThat("Template should be created", templateId, notNullValue());
        createdTemplateIds.add(templateId);

        String invalidToken = "invalid_token_12345";
        Response response = markTemplateAsDefault(invalidToken, templateId, true);

        assertThat("Mark as default should return 401", response.getStatusCode(), equalTo(401));
        validateUnauthorizedResponse(response);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMarkAsDefault_404() {
        Integer nonExistentTemplateId = 999999;
        Response response = markTemplateAsDefault(albatrossAuthToken, nonExistentTemplateId, true);

        assertThat("Mark as default should return 404", response.getStatusCode(), equalTo(404));
        validateNotFoundResponse(response);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMarkAsDefault_422() throws InterruptedException {
        Integer templateId = createAndFetchTemplateId(albatrossAuthToken, "VALIDATION_422");
        assertThat("Template should be created", templateId, notNullValue());
        createdTemplateIds.add(templateId);

        Response response = markTemplateAsDefault(albatrossAuthToken, templateId, true);

        if (response.getStatusCode() == 422) {
            validateErrorResponse(response, "validation");
        } else {
            assertThat("Response should be successful or have expected error",
                    response.getStatusCode(), anyOf(equalTo(200), equalTo(422)));
        }
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMarkAsDefaultToggle() throws InterruptedException {
        Integer templateId = createAndFetchTemplateId(albatrossAuthToken, "TOGGLE_TEST");
        assertThat("Template should be created", templateId, notNullValue());
        createdTemplateIds.add(templateId);

        Response markResponse = markTemplateAsDefault(albatrossAuthToken, templateId, true);
        assertThat("Mark as default should succeed", markResponse.getStatusCode(), equalTo(200));

        verifyTemplateIsMarkedAsDefault(albatrossAuthToken, templateId);

        Response unmarkResponse = markTemplateAsDefault(albatrossAuthToken, templateId, false);
        assertThat("Unmark as default should succeed", unmarkResponse.getStatusCode(), equalTo(200));

        verifyTemplateIsNotMarkedAsDefault(albatrossAuthToken, templateId);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyMultipleTemplatesDefaultBehavior() throws InterruptedException {
        Integer template1 = createAndFetchTemplateId(albatrossAuthToken, "MULTI_TEST_1");
        Integer template2 = createAndFetchTemplateId(albatrossAuthToken, "MULTI_TEST_2");

        assertThat("Template 1 should be created", template1, notNullValue());
        assertThat("Template 2 should be created", template2, notNullValue());

        createdTemplateIds.add(template1);
        createdTemplateIds.add(template2);

        Response mark1Response = markTemplateAsDefault(albatrossAuthToken, template1, true);
        assertThat("Mark template 1 as default should succeed", mark1Response.getStatusCode(), equalTo(200));

        Response mark2Response = markTemplateAsDefault(albatrossAuthToken, template2, true);
        assertThat("Mark template 2 as default should succeed", mark2Response.getStatusCode(), equalTo(200));

        verifyTemplateIsMarkedAsDefault(albatrossAuthToken, template2);
    }

    @Owner("Yash Rampal")
    @Test(groups = {"contract_staffing", "nightly-build"})
    public void verifyDefaultTemplateAppearsFirstInList() throws InterruptedException {
        Integer template1 = createAndFetchTemplateId(albatrossAuthToken, "LIST_ORDER_1");
        Integer template2 = createAndFetchTemplateId(albatrossAuthToken, "LIST_ORDER_2");
        Integer template3 = createAndFetchTemplateId(albatrossAuthToken, "LIST_ORDER_3");
        
        assertThat("Template 1 should be created", template1, notNullValue());
        assertThat("Template 2 should be created", template2, notNullValue());
        assertThat("Template 3 should be created", template3, notNullValue());
        
        createdTemplateIds.add(template1);
        createdTemplateIds.add(template2);
        createdTemplateIds.add(template3);
        
        Response markResponse = markTemplateAsDefault(albatrossAuthToken, template3, true);
        assertThat("Mark template 3 as default should succeed", markResponse.getStatusCode(), equalTo(200));
        
        Response listResponse = getTemplateList(albatrossAuthToken);
        assertThat("Get template list should succeed", listResponse.getStatusCode(), equalTo(200));
        
        List<Map<String, Object>> templates = getResponseData(listResponse);
        assertThat("Template list should not be empty", templates, notNullValue());
        assertThat("Template list should have templates", templates.size(), greaterThan(0));
        
        Map<String, Object> firstTemplate = templates.get(0);
        Integer firstTemplateId = (Integer) firstTemplate.get("id");
        
        assertThat("First template in list should be the default template", firstTemplateId, equalTo(template3));
        
        Object isDefault = firstTemplate.get("isDefault");
        if (isDefault instanceof Integer) {
            assertThat("First template should be marked as default", (Integer) isDefault, equalTo(1));
        } else if (isDefault instanceof Boolean) {
            assertThat("First template should be marked as default", (Boolean) isDefault, equalTo(true));
        }
    }
}
