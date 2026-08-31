package io.rcrm.api.nyma;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.nyma.ConnectEmailPage;
import io.rcrm.api.pojo.nyma.EmailSettingUpdateFieldsPage;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EmailSettingsTest extends TestBase {

    @Owner("Ajendra Singh")
    @Test(priority = 0, groups = "nightly-build")
    public void connectEmail(){
        for(int i=1; i<=2; i++) {
            ConnectEmailPage connectEmailPage = new ConnectEmailPage(i);
            Response response = RestClient.doPost("JSON", nymaURLv3, "signin/email", ThreadManager.getOwnerAlbatrossToken(), null, true, connectEmailPage);
            Assert.assertEquals(response.getStatusCode(), 200);
            Assert.assertNotNull(response.jsonPath().get("data.auth_url"));
        }
    }

    @Owner("Harika")
    @Test(priority = 1, groups = "nightly-build")
    public void getConnectedEmails(){
        connectToRandomEmail(ThreadManager.getAccount().getAccountId(), 1, 1, 4, null);
        connectToRandomEmail(ThreadManager.getAccount().getAccountId(), 2, 0, 4, ThreadManager.getOwner().getConnectedEmail_1());

        Response response = RestClient.doPost("JSON", nymaURLv3, "/connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath userEmails = response.jsonPath();
        Assert.assertNotNull(userEmails.get("user.email_1"));
        Assert.assertNotNull(userEmails.get("user.email_2"));
        Assert.assertEquals(userEmails.get("user.email_1.senderemail"), ThreadManager.getOwner().getConnectedEmail_1());
        Assert.assertEquals(userEmails.get("user.email_2.senderemail"), ThreadManager.getOwner().getConnectedEmail_2());
    }

    @Owner("Ajendra Singh")
    @Test(priority = 2, groups = "nightly-build")
    public void verifyHideEmailAndToggleDefault(){
        String keys[] = {"hide_mails", "is_default"};

        for(String key: keys){
            EmailSettingUpdateFieldsPage emailSettingUpdateFieldsPage = new EmailSettingUpdateFieldsPage(key, 1, 1);
            Response response = RestClient.doPost("JSON", nymaURLv3, "/update-fields", ThreadManager.getOwnerAlbatrossToken(), null, true, emailSettingUpdateFieldsPage);
            JsonPath responseJson = response.jsonPath();

            Assert.assertEquals(response.getStatusCode(), 200);
            Assert.assertEquals(responseJson.get("status"), "success");
            Assert.assertEquals(responseJson.get("message"), "Field Updated Successfully");
            Assert.assertEquals(responseJson.getInt("user.email_1."+key), 1);
        }
    }

    @Owner("Harika")
    @Test(priority = 3, groups = "nightly-build")
    public void disconnectEmail(){
        Response response = RestClient.doPost("JSON", nymaURLv3, "/connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath userEmails = response.jsonPath();
        Assert.assertNotNull(userEmails.get("user.email_1"));
        Assert.assertNotNull(userEmails.get("user.email_2"));

        String basePath = "/disconnect/{id}";
        HashMap<String, String> pathParameters = null;
        for(int i=1; i<=2; i++){
            pathParameters = new HashMap<>();
            pathParameters.put("id", userEmails.get("user.email_"+i+".emailsettingid").toString());
            Response response1 = RestClient.doDelete("JSON", nymaURLv3, basePath, ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
            JsonPath response1Json = response1.jsonPath();

            Assert.assertEquals(response1.getStatusCode(), 200);
            Assert.assertEquals(response1Json.get("status"), "success");
            Assert.assertEquals(response1Json.get("message"), "Email Settings Deleted");
        }
    }

    @Owner("Ajendra Singh")
    @Test(priority = 4, groups = "nightly-build")
    public void verifyNotifyWhenEmailAutoDisconnected(){
        String connectedEmail = connectToRandomEmail(ThreadManager.getAccount().getAccountId(), 1, 1, 4, null);
        disconnectNylasEmail(ThreadManager.getAccount().getAccountId(), 1, 1);

        Response response = RestClient.doPost("JSON", nymaURLv3, "/connected-emails", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath userEmails = response.jsonPath();
        Assert.assertNull(userEmails.get("user.email_1"));
        Assert.assertEquals(userEmails.get("data.notify[0].senderemail"), connectedEmail);
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void connectEmailWithoutLinkedEmailType(){
        Response response = RestClient.doPost("JSON", nymaURLv3, "/signin/email", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "The linked email type field is required.");
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void connectEmailWithInvalidLinkedEmailType(){
        ConnectEmailPage connectEmailPage = new ConnectEmailPage(0);
        Response response = RestClient.doPost("JSON", nymaURLv3, "/signin/email", ThreadManager.getOwnerAlbatrossToken(), null, true, connectEmailPage);
        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "The selected linked email type is invalid.");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void connectEmailInvalidAuthTest(){
        ConnectEmailPage connectEmailPage = new ConnectEmailPage(1);
        Response response = RestClient.doPost("JSON", nymaURLv3, "/signin/email", ThreadManager.getOwnerAlbatrossToken()+"1234", null, true, connectEmailPage);
        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void disconnectEmailWithInvalidId(){
        HashMap<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "xyz");
        Response response = RestClient.doDelete("JSON", nymaURLv3, "/disconnect/{id}", ThreadManager.getOwnerAlbatrossToken(), null, pathParameters, true);
        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(response.jsonPath().get("status"), "fail");
        Assert.assertEquals(response.jsonPath().get("message"), "The id must be an integer.");
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void disconnectEmailInvalidAuthTest(){
        HashMap<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "1");
        Response response = RestClient.doDelete("JSON", nymaURLv3, "/disconnect/{id}", ThreadManager.getOwnerAlbatrossToken()+"123", null, pathParameters, true);
        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void getConnectedEmailsInvalidAuthTest(){
        Response response = RestClient.doPost("JSON", nymaURLv3, "/connected-emails", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, null);
        Assert.assertEquals(response.getStatusCode(), 401);
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void verifyUpdateFieldsWithoutRequiredFields(){
        Response response = RestClient.doPost("JSON", nymaURLv3, "/update-fields", ThreadManager.getOwnerAlbatrossToken(), null, true, null);
        JsonPath responseJson = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(responseJson.get("status"), "fail");
        Assert.assertTrue(responseJson.getString("message").contains("The key field is required."));
        Assert.assertTrue(responseJson.getString("message").contains("The value field is required."));
        Assert.assertTrue(responseJson.getString("message").contains("The linked email type field is required."));
    }

    @Owner("Ajendra Singh")
    @Test(groups = "nightly-build")
    public void verifyUpdateFieldsWithInvalidFields(){
        EmailSettingUpdateFieldsPage emailSettingUpdateFieldsPage = new EmailSettingUpdateFieldsPage("xyz", 2, 0);
        Response response = RestClient.doPost("JSON", nymaURLv3, "/update-fields", ThreadManager.getOwnerAlbatrossToken(), null, true, emailSettingUpdateFieldsPage);
        JsonPath responseJson = response.jsonPath();
        Assert.assertEquals(response.getStatusCode(), 422);
        Assert.assertEquals(responseJson.get("status"), "fail");
        Assert.assertTrue(responseJson.getString("message").contains("The selected key is invalid."));
        Assert.assertTrue(responseJson.getString("message").contains("The selected value is invalid."));
        Assert.assertTrue(responseJson.getString("message").contains("The selected linked email type is invalid."));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void updateFieldsInvalidAuthTest(){
        EmailSettingUpdateFieldsPage emailSettingUpdateFieldsPage = new EmailSettingUpdateFieldsPage("hide_mails", 1, 1);
        Response response = RestClient.doPost("JSON", nymaURLv3, "/update-fields", ThreadManager.getOwnerAlbatrossToken()+"123", null, true, emailSettingUpdateFieldsPage);
        Assert.assertEquals(response.getStatusCode(), 401);
    }

}
