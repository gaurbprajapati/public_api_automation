package io.recruitcrm.albatross.emailTemplate;

import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.reaper.ReaperIntegration;
import io.rcrm.api.commanfunctions.albatross.RBAC4LevelAccessDataProvider;
import io.rcrm.api.javafaker.JavaFakerMails;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.pojo.albatross.DeleteTemplatePage;
import io.rcrm.api.pojo.reaper.CreateEmailTemplateRequestBody;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACEmailTemplateDeleteSecurityTest extends TestBase {

    private final JavaFakerMails fakerMails = new JavaFakerMails();

    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    private int createEmailTemplateWithPermissions(Boolean shareWithEveryone, String creator) {
        CreateEmailTemplateRequestBody requestBody = new CreateEmailTemplateRequestBody();
        requestBody.setEmailcontext("Email Template " + RandomStringUtils.randomAlphabetic(4));
        requestBody.setEmailsubject("AJ testing automation");
        requestBody.setTemplate(fakerMails.getFakeEmailBody(5));
        requestBody.setAccountid(ThreadManager.getAccount().getAccountId());

        requestBody.setCreatedby(userIdsMap.get(creator));
        requestBody.setRelatedtotypeid(5);
        requestBody.setShare(shareWithEveryone ? 1 : 0); // Convert Boolean to Integer (0 = private, 1 = shared)

        Response response = ReaperIntegration.createEmailTemplate(requestBody);
        response.then().statusCode(200);
        String idPart = response.getBody().asString().split("ID:")[1].trim();
        return Integer.parseInt(idPart);
    }

    private Response deleteEmailTemplate(int templateId, String token) {
        DeleteTemplatePage deleteTemplatePage = new DeleteTemplatePage();
        deleteTemplatePage.setIdsToDelete(templateId);
        deleteTemplatePage.setTableFlag("email_template");
        return RestClient.doPost("JSON", albatrossURL, "global/delete-record", token, null, true, deleteTemplatePage);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage) {
        if (expectedStatusCode == 200) {
            switch (expectedMessage) {
                case "Forbidden":
                    response.then().body("message", Matchers.is("Access Denied"));
                    break;
                case "Success":
                    response.then().body("message", Matchers.is("Email Template Deleted"));
                    break;
                default:
                    System.out.println("Unexpected expected message: " + expectedMessage);
            }
        } else if (expectedStatusCode == 401) {
            if ("Forbidden".equals(expectedMessage)) {
                response.then().body("error", Matchers.is("Unauthorized"));
            }
        }
    }

    @Owner("Ajendra Singh")
    @Test(dataProvider = "emailTemplateDeleteAccessData", groups = {"role-based", "email-template-delete-access"})
    public void deleteEmailTemplate_Test(String creator, String executor, String shareType, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executor);

        int templateId = createEmailTemplateWithPermissions("shared".equals(shareType), creator);
        Response deleteResponse = deleteEmailTemplate(templateId, executorToken);

        validateResponse(deleteResponse, expectedStatusCode, expectedMessage);
    }

    @DataProvider(name = "emailTemplateDeleteAccessData")
    public Object[][] emailTemplateDeleteAccessData(ITestContext context) {
        return RBAC4LevelAccessDataProvider.getEmailTemplateAccessData(context);
    }
}
