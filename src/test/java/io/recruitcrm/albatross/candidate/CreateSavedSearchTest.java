package io.recruitcrm.albatross.candidate;

import io.rcrm.api.javafaker.JavaFakerSavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearch;
import io.rcrm.api.pojo.albatross.SavedSearchRequest;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import com.qa.api.util.reaper.ThreadManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import com.qa.api.util.Owner;

@TestBase.AccountType("Business|AlbatrossTkn")
public class CreateSavedSearchTest extends TestBase {

    private String albatrossAuthToken;
    private final String SAVED_SEARCH_ENDPOINT = "saved-searches";
    private JavaFakerSavedSearch fakerSavedSearch;
    private String searchName;
    private String jsonTemplate;

    @BeforeClass(alwaysRun = true)    public void setUp() {
        albatrossAuthToken = ThreadManager.getOwnerAlbatrossToken();
        fakerSavedSearch = new JavaFakerSavedSearch();
        searchName = fakerSavedSearch.getSavedSearchName();
        jsonTemplate = readJsonFromResource("saved-search-template.json");
    }

    private String readJsonFromResource(String resourceName) {
        try {
            return new String(((java.io.InputStream) getClass().getClassLoader().getResourceAsStream(resourceName))
                    .readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourceName, e);
        }
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void testCreateSavedSearch_ValidRequest_200() {
        // Arrange
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(searchName);
        savedSearch.setEntitytype("candidates");
        savedSearch.setJson(jsonTemplate);
        savedSearch.setUserid(null);
        savedSearch.setAccountid(null);
        savedSearch.setShare_with_teammates(0);
        savedSearch.setPost_search_revamp(1);

        List<Object> collaboratorIds = new ArrayList<>();
        List<Object> collaboratorTypes = new ArrayList<>();
        savedSearch.setCollaborator_id(collaboratorIds);
        savedSearch.setCollaborator_type(collaboratorTypes);

        SavedSearchRequest savedSearchRequest = new SavedSearchRequest();
        savedSearchRequest.setSave_searches(savedSearch);
        savedSearchRequest.setUpdateUserObj(false);

        // Act
        Response response = RestClient.doPost("JSON", albatrossURL, SAVED_SEARCH_ENDPOINT, authTokenMap, null, true,
                savedSearchRequest);

        // Assert
        JsonPath jsonPath = response.jsonPath();
        assertThat("Status code should be 200", response.getStatusCode(), equalTo(200));
        assertThat("Response should contain success message", jsonPath.getString("message"),
                containsString("Setting Save Successful"));
        assertThat("Response should have a message_type", jsonPath.getString("message_type"), equalTo("is-success"));
        assertThat("Saved search name should match", jsonPath.getString("data.name"), equalTo(searchName));
        assertThat("Entity type should be candidates", jsonPath.getString("data.entitytype"), equalTo("candidates"));
        assertThat("ID should be present", jsonPath.getInt("data.id"), greaterThan(0));
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void testCreateSavedSearch_UnauthorizedAccess_401() {
        // Arrange
        Map<String, String> invalidAuthMap = new HashMap<>();
        invalidAuthMap.put("Authorization", "Bearer eyJ0eXAiOi");

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(searchName);
        savedSearch.setEntitytype("candidates");
        savedSearch.setJson(jsonTemplate);
        savedSearch.setUserid(null);
        savedSearch.setAccountid(null);
        savedSearch.setShare_with_teammates(0);
        savedSearch.setPost_search_revamp(1);

        List<Object> collaboratorIds = new ArrayList<>();
        List<Object> collaboratorTypes = new ArrayList<>();
        savedSearch.setCollaborator_id(collaboratorIds);
        savedSearch.setCollaborator_type(collaboratorTypes);

        SavedSearchRequest savedSearchRequest = new SavedSearchRequest();
        savedSearchRequest.setSave_searches(savedSearch);
        savedSearchRequest.setUpdateUserObj(false);

        // Act
        Response response = RestClient.doPost("JSON", albatrossURL, SAVED_SEARCH_ENDPOINT, invalidAuthMap, null, true,
                savedSearchRequest);

        // Assert
        assertThat("Status code should be 401", response.getStatusCode(), equalTo(401));
        // Additional assertions about error message if needed
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void testCreateSavedSearch_EmptyRequestBody_500() {
        // Arrange
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        // Send empty request body
        SavedSearchRequest savedSearchRequest = new SavedSearchRequest();

        // Act
        Response response = RestClient.doPostOnce("JSON", albatrossURL, SAVED_SEARCH_ENDPOINT, authTokenMap, null, true,
                savedSearchRequest);

        // Assert
        assertThat("Status code should be 400 for empty request",
                response.getStatusCode(),
                equalTo(500));
    }

    @Owner("Yash Rampal")
    @Test(groups = "nightly-build")
    public void testCreateSavedSearch_InvalidHttpMethod_405() {
        // Arrange
        Map<String, String> authTokenMap = getAuthTokenMap(albatrossAuthToken);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setName(searchName);
        savedSearch.setEntitytype("candidates");
        savedSearch.setJson(jsonTemplate);
        savedSearch.setUserid(null);
        savedSearch.setAccountid(null);
        savedSearch.setShare_with_teammates(0);
        savedSearch.setPost_search_revamp(1);

        List<Object> collaboratorIds = new ArrayList<>();
        List<Object> collaboratorTypes = new ArrayList<>();
        savedSearch.setCollaborator_id(collaboratorIds);
        savedSearch.setCollaborator_type(collaboratorTypes);

        SavedSearchRequest savedSearchRequest = new SavedSearchRequest();
        savedSearchRequest.setSave_searches(savedSearch);
        savedSearchRequest.setUpdateUserObj(false);

        // Act
        Response response = RestClient.doPut("JSON", albatrossURL, SAVED_SEARCH_ENDPOINT, authTokenMap, null, true,
                savedSearchRequest);

        // Assert
        assertThat("Status code should be 405 Method Not Allowed or similar error",
                response.getStatusCode(),
                anyOf(equalTo(405), greaterThan(400)));
    }

    private Map<String, String> getAuthTokenMap(Object authToken) {
        Map<String, String> authTokenMap;
        if (authToken instanceof Map) {
            authTokenMap = (Map<String, String>) authToken;
        } else {
            String apiKey = (String) authToken;
            authTokenMap = new HashMap<>();
            authTokenMap.put("Authorization", "Bearer " + apiKey);
        }
        return authTokenMap;
    }
}