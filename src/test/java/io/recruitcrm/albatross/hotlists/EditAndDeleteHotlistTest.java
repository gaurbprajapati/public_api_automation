package io.recruitcrm.albatross.hotlists;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.qa.api.util.reaper.ThreadManager;

import io.rcrm.api.javafaker.JavaFakerHotlist;
import io.rcrm.api.pojo.Hotlist;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class EditAndDeleteHotlistTest extends TestBase {
    
    private JavaFakerHotlist fakerHotlist = new JavaFakerHotlist();
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)	public void setUp() {
		albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
	}
     
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testEditHotlistForCompany() {
        String originalHotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(originalHotlistName);
        createHotlist.setRelated_to_type("company");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        
        String updatedHotlistName = "Updated Company Hotlist";
        
        Hotlist editHotlist = new Hotlist();
        editHotlist.setFirst_name(updatedHotlistName);
        editHotlist.setShared(1);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}";
        
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, basePath, 
            albatrossTkn, null, pathParameters, true, editHotlist);
            
        editResponse.prettyPrint();
        
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        Assert.assertEquals(editResponse.jsonPath().getString("message"), "Update Hotlist Successful");
        Assert.assertEquals(editResponse.jsonPath().getString("message_type"), "is-success");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testEditHotlistForContact() {
        String originalHotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(originalHotlistName);
        createHotlist.setRelated_to_type("contact");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        
        String updatedHotlistName = "Updated Contact Hotlist";
        
        Hotlist editHotlist = new Hotlist();
        editHotlist.setFirst_name(updatedHotlistName);
        editHotlist.setShared(1);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}";
        
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, basePath, 
            albatrossTkn, null, pathParameters, true, editHotlist);
            
        editResponse.prettyPrint();
        
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        Assert.assertEquals(editResponse.jsonPath().getString("message"), "Update Hotlist Successful");
        Assert.assertEquals(editResponse.jsonPath().getString("message_type"), "is-success");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testEditHotlistForJob() {
        String originalHotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(originalHotlistName);
        createHotlist.setRelated_to_type("job");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        
        String updatedHotlistName = "Updated Job Hotlist";
        
        Hotlist editHotlist = new Hotlist();
        editHotlist.setFirst_name(updatedHotlistName);
        editHotlist.setShared(1);
        
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("hotlist", String.valueOf(hotlistId));
        String basePath = "hotlists/{hotlist}";
        
        Response editResponse = RestClient.doPost1("JSON", albatrossURL, basePath, 
            albatrossTkn, null, pathParameters, true, editHotlist);
            
        editResponse.prettyPrint();
        
        Assert.assertEquals(editResponse.getStatusCode(), 200);
        Assert.assertEquals(editResponse.jsonPath().getString("message"), "Update Hotlist Successful");
        Assert.assertEquals(editResponse.jsonPath().getString("message_type"), "is-success");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testDeleteHotlistForCompany() {
        String hotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(hotlistName);
        createHotlist.setRelated_to_type("company");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        System.out.println("Created company hotlist with ID: " + hotlistId + " for deletion");
        
        String fullUrl = albatrossURL + "/hotlists/delete";
        
        Response deleteResponse = RestAssured.given()
            .log().all()
            .header("Authorization", "Bearer " + albatrossTkn)
            .contentType(ContentType.JSON)
            .accept("application/json")
            .urlEncodingEnabled(false)
            .queryParam("hotlistId[]", hotlistId)
            .queryParam("entityname", "companies")
            .when()
            .delete(fullUrl);
        
        deleteResponse.prettyPrint();
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        
        JsonPath jsonPath = deleteResponse.jsonPath();
        
        Assert.assertEquals(jsonPath.getString("action_name"), "Delete Hotlist");
        Assert.assertEquals(jsonPath.getString("message"), "Delete Hotlist Successful");
        Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
        Assert.assertEquals(jsonPath.getString("status"), "success");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testDeleteHotlistForContact() {
        String hotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(hotlistName);
        createHotlist.setRelated_to_type("contact");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        System.out.println("Created contact hotlist with ID: " + hotlistId + " for deletion");
        
        String fullUrl = albatrossURL + "/hotlists/delete";
        
        Response deleteResponse = RestAssured.given()
            .log().all()
            .header("Authorization", "Bearer " + albatrossTkn)
            .contentType(ContentType.JSON)
            .accept("application/json")
            .urlEncodingEnabled(false)
            .queryParam("hotlistId[]", hotlistId)
            .queryParam("entityname", "contacts")
            .when()
            .delete(fullUrl);
        
        deleteResponse.prettyPrint();
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        
        JsonPath jsonPath = deleteResponse.jsonPath();
        
        Assert.assertEquals(jsonPath.getString("action_name"), "Delete Hotlist");
        Assert.assertEquals(jsonPath.getString("message"), "Delete Hotlist Successful");
        Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
        Assert.assertEquals(jsonPath.getString("status"), "success");
    }
    
    @Owner("Rahul Shibu")
    @Test(groups = "nightly-build")
    public void testDeleteHotlistForJob() {
        String hotlistName = fakerHotlist.getHotlistName();
        
        Hotlist createHotlist = new Hotlist();
        createHotlist.setFirst_name(hotlistName);
        createHotlist.setRelated_to_type("job");
        createHotlist.setShared(1);
        
        Response createResponse = RestClient.doPost("JSON", baseURL, "hotlists", 
            ThreadManager.getAccountApiKey(), null, true, createHotlist);
        
        int hotlistId = createResponse.jsonPath().getInt("id");
        System.out.println("Created job hotlist with ID: " + hotlistId + " for deletion");
        
        String fullUrl = albatrossURL + "/hotlists/delete";
        
        Response deleteResponse = RestAssured.given()
            .log().all()
            .header("Authorization", "Bearer " + albatrossTkn)
            .contentType(ContentType.JSON)
            .accept("application/json")
            .urlEncodingEnabled(false)
            .queryParam("hotlistId[]", hotlistId)
            .queryParam("entityname", "jobs")
            .when()
            .delete(fullUrl);
        
        deleteResponse.prettyPrint();
        Assert.assertEquals(deleteResponse.getStatusCode(), 200);
        
        JsonPath jsonPath = deleteResponse.jsonPath();
        
        Assert.assertEquals(jsonPath.getString("action_name"), "Delete Hotlist");
        Assert.assertEquals(jsonPath.getString("message"), "Delete Hotlist Successful");
        Assert.assertEquals(jsonPath.getString("message_type"), "is-success");
        Assert.assertEquals(jsonPath.getString("status"), "success");
    }
    
}
