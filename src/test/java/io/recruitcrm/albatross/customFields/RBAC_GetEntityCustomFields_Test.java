package io.recruitcrm.albatross.customFields;

import org.testng.annotations.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import org.hamcrest.Matchers;
import org.testng.Assert;
import java.util.*;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.albatross.AllCrudFunctions;
import io.rcrm.api.pojo.albatross.*;
import io.restassured.path.json.JsonPath;
import com.qa.api.util.reaper.ThreadManager;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBAC_GetEntityCustomFields_Test extends TestBase {
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    private final String basePath = "entity-custom-fields/get";
    commanFunction function = new commanFunction();
    AllCrudFunctions privateFunction = new AllCrudFunctions();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    @Owner("Smit Patel")
    @Test(dataProvider = "rbacGetEntityCustomFieldsAccessData", groups = {"rbac", "get-entity-custom-fields"})
    public void getEntityCustomFieldsRBACAccess_Test(String executorRole, String entityType, Integer customFieldColId, Integer recordId, String entitySlug, int expectedStatusCode, String expectedMessage, String testDescription) {
        String executorToken = albatrossTknMap.get(executorRole);
        EntityTypeCustomField requestBody = createRequestBody(entityType, customFieldColId, recordId);
        Response response = RestClient.doPost("JSON", albatrossURL, basePath, executorToken, null, false, requestBody);
        validateResponse(response, expectedStatusCode, expectedMessage, testDescription, entityType, entitySlug);
    }

    private void validateResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription, String entityType, String entitySlug) {
        int actualStatusCode = response.getStatusCode();
        try {
            response.then().statusCode(expectedStatusCode);
        } catch (AssertionError e) {
            throw new AssertionError("Test Case FAILED: " + testDescription + " - Expected Status: " + expectedStatusCode + ", Got: " + actualStatusCode, e);
        }
        if (expectedStatusCode == SUCCESS_STATUS_CODE && SUCCESS_MESSAGE.equals(expectedMessage)) {
            validateSuccessResponse(response, entityType, entitySlug);
        }
    }

    private void validateSuccessResponse(Response response, String entityType, String entitySlug) {
        response.then().body("message_type", Matchers.equalToIgnoringCase("is-success"));
        response.then().body("message", Matchers.equalToIgnoringCase("Fetched custom field records"));
        String dataPath = getDataPath(entityType, entitySlug);
        response.then().body(dataPath, Matchers.notNullValue());
    }

    private String getDataPath(String entityType, String entitySlug) {
        switch (entityType) {
            case "candidate":
                return "data.candidate['" + entitySlug + "'].id";
            case "company":
                return "data.company['" + entitySlug + "'].id";
            case "contact":
                return "data.contact['" + entitySlug + "'].id";
            case "deal":
                return "data.deals['" + entitySlug + "'].id";
            case "job":
                return "data.job['" + entitySlug + "'].id";
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    private EntityTypeCustomField createRequestBody(String entityType, Integer customFieldColId, Integer recordId) {
        EntityTypeCustomField requestBody = new EntityTypeCustomField();
        requestBody.setEntityTypeId(3);
        requestBody.setRecordIds(Collections.singletonList(recordId));
        
        switch (entityType) {
            case "candidate":
                requestBody.setCandidateCustomFieldIds(Collections.singletonList("custcolumn" + customFieldColId));
                break;
            case "company":
                requestBody.setCompanyCustomFieldIds(Collections.singletonList("custcolumn" + customFieldColId));
                break;
            case "contact":
                requestBody.setContactCustomFieldIds(Collections.singletonList("custcolumn" + customFieldColId));
                break;
            case "deal":
                requestBody.setDealCustomFieldIds(Collections.singletonList("custcolumn" + customFieldColId));
                break;
            case "job":
                requestBody.setJobCustomFieldIds(Collections.singletonList("custcolumn" + customFieldColId));
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        return requestBody;
    }

    @DataProvider(name = "rbacGetEntityCustomFieldsAccessData", parallel = true)
    public Object[][] rbacGetEntityCustomFieldsAccessData() {
        Object[][] candidateData = getEntityCustomFieldData("candidate");
        Object[][] companyData = getEntityCustomFieldData("company");
        Object[][] contactData = getEntityCustomFieldData("contact");
        Object[][] dealData = getEntityCustomFieldData("deal");
        Object[][] jobData = getEntityCustomFieldData("job");
        
        Integer candColId = (Integer) candidateData[0][0];
        Integer candRecordId = (Integer) candidateData[0][1];
        String candSlug = (String) candidateData[0][2];
        
        Integer compColId = (Integer) companyData[0][0];
        Integer compRecordId = (Integer) companyData[0][1];
        String compSlug = (String) companyData[0][2];
        
        Integer contColId = (Integer) contactData[0][0];
        Integer contRecordId = (Integer) contactData[0][1];
        String contSlug = (String) contactData[0][2];
        
        Integer dealColId = (Integer) dealData[0][0];
        Integer dealRecordId = (Integer) dealData[0][1];
        String dealSlug = (String) dealData[0][2];
        
        Integer jobColId = (Integer) jobData[0][0];
        Integer jobRecordId = (Integer) jobData[0][1];
        String jobSlug = (String) jobData[0][2];
        
        return new Object[][] {
            // ERB created for Restricted Team Member access - TITAN-21397
            
            // Candidate entity scenarios
            {"AccountOwner", "candidate", candColId, candRecordId, candSlug, 200, "Success", "Owner Token can get candidate entity custom fields - TC001"},
            {"Admin", "candidate", candColId, candRecordId, candSlug, 200, "Success", "Admin Token can get candidate entity custom fields - TC002"},
            {"TeamMember", "candidate", candColId, candRecordId, candSlug, 200, "Success", "TeamMember Token can get candidate entity custom fields - TC003"},
            {"RestrictedTeamMember", "candidate", candColId, candRecordId, candSlug, 200, "Success", "Restricted Team Member Token can get candidate entity custom fields - TC004"},
            {"CustomRoleTeamOnly", "candidate", candColId, candRecordId, candSlug, 200, "Success", "Custom Role Team Only Token can get candidate entity custom fields - TC005"},
            {"CustomRoleNothing", "candidate", candColId, candRecordId, candSlug, 200, "Success", "Custom Role Nothing Token can get candidate entity custom fields - TC006"},
            
            // Company entity scenarios
            {"AccountOwner", "company", compColId, compRecordId, compSlug, 200, "Success", "Owner Token can get company entity custom fields - TC007"},
            {"Admin", "company", compColId, compRecordId, compSlug, 200, "Success", "Admin Token can get company entity custom fields - TC008"},
            {"TeamMember", "company", compColId, compRecordId, compSlug, 200, "Success", "TeamMember Token can get company entity custom fields - TC009"},
            {"RestrictedTeamMember", "company", compColId, compRecordId, compSlug, 200, "Success", "Restricted Team Member Token can get company entity custom fields - TC010"},
            {"CustomRoleTeamOnly", "company", compColId, compRecordId, compSlug, 200, "Success", "Custom Role Team Only Token can get company entity custom fields - TC011"},
            {"CustomRoleNothing", "company", compColId, compRecordId, compSlug, 200, "Success", "Custom Role Nothing Token can get company entity custom fields - TC012"},
            
            // Contact entity scenarios
            {"AccountOwner", "contact", contColId, contRecordId, contSlug, 200, "Success", "Owner Token can get contact entity custom fields - TC013"},
            {"Admin", "contact", contColId, contRecordId, contSlug, 200, "Success", "Admin Token can get contact entity custom fields - TC014"},
            {"TeamMember", "contact", contColId, contRecordId, contSlug, 200, "Success", "TeamMember Token can get contact entity custom fields - TC015"},
            {"RestrictedTeamMember", "contact", contColId, contRecordId, contSlug, 200, "Success", "Restricted Team Member Token can get contact entity custom fields - TC016"},
            {"CustomRoleTeamOnly", "contact", contColId, contRecordId, contSlug, 200, "Success", "Custom Role Team Only Token can get contact entity custom fields - TC017"},
            {"CustomRoleNothing", "contact", contColId, contRecordId, contSlug, 200, "Success", "Custom Role Nothing Token can get contact entity custom fields - TC018"},
            
            // Deal entity scenarios
            {"AccountOwner", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "Owner Token can get deal entity custom fields - TC019"},
            {"Admin", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "Admin Token can get deal entity custom fields - TC020"},
            {"TeamMember", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "TeamMember Token can get deal entity custom fields - TC021"},
            {"RestrictedTeamMember", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "Restricted Team Member Token can get deal entity custom fields - TC022"},
            {"CustomRoleTeamOnly", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "Custom Role Team Only Token can get deal entity custom fields - TC023"},
            {"CustomRoleNothing", "deal", dealColId, dealRecordId, dealSlug, 200, "Success", "Custom Role Nothing Token can get deal entity custom fields - TC024"},
            
            // Job entity scenarios
            {"AccountOwner", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "Owner Token can get job entity custom fields - TC025"},
            {"Admin", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "Admin Token can get job entity custom fields - TC026"},
            {"TeamMember", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "TeamMember Token can get job entity custom fields - TC027"},
            {"RestrictedTeamMember", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "Restricted Team Member Token can get job entity custom fields - TC028"},
            {"CustomRoleTeamOnly", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "Custom Role Team Only Token can get job entity custom fields - TC029"},
            {"CustomRoleNothing", "job", jobColId, jobRecordId, jobSlug, 200, "Success", "Custom Role Nothing Token can get job entity custom fields - TC030"}
        };
    }

    public Object[][] getEntityCustomFieldData(String entityType) {
        String ownerToken = albatrossTknMap.get("AccountOwner");
        String apiAuthToken = ThreadManager.getAccountApiKey();
        
        int entityId;
        String entitySlug;
        String companySlug = createEntity("company", apiAuthToken);
        int companyID = getEntityId("company", companySlug, ownerToken);
        
        if (entityType.equals("candidate")) {
            entitySlug = createEntity("candidate", apiAuthToken);
            entityId = createCustomField("company", "candidateField", "Candidate", ownerToken);
        } else if (entityType.equals("company")) {
            entitySlug = createEntity("company", apiAuthToken);
            entityId = createCustomField("company", "companyField", "Company", ownerToken);
        } else if (entityType.equals("contact")) {
            entitySlug = createEntity("contact", apiAuthToken);
            entityId = createCustomField("company", "contactField", "Contact", ownerToken);
        } else if (entityType.equals("deal")) {
            entitySlug = createEntity("deal", apiAuthToken);
            entityId = createCustomField("company", "dealField", "Deal", ownerToken);
        } else {
            entitySlug = createEntity("job", apiAuthToken);
            entityId = createCustomField("company", "jobField", "Job", ownerToken);
        }
        
        updateCustomField("company", companyID, ownerToken, "custcolumn" + entityId, entitySlug);
        Object data[][] = {{ entityId, companyID, entitySlug }};
        return data;
    }

    public void updateCustomField(String entityType, int entityId, String albatrossAuthToken, String key, String value) {
        List<Integer> entityIds = Arrays.asList(entityId);
        UpdateFields updateFields = new UpdateFields();
        updateFields.setKey(key);
        updateFields.setValue(value);
        updateFields.setTableFlag(entityType);
        updateFields.setId(entityIds);
        Response response = RestClient.doPost("JSON", albatrossURL, "global/update-fields", albatrossAuthToken, null, true, updateFields);
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    public int createCustomField(String entityName, String customFieldName, String customFieldType, String albatrossAuthToken) {
        Response resp = function.createCustomFieldsResponse(albatrossURL, albatrossAuthToken, entityName, customFieldName, customFieldType, "");
        int id = resp.jsonPath().get("data.custumField.columnid");
        return id;
    }

    public String createEntity(String relatedToType, String apiAuthToken) {
        String entitySlug = null;
        if (relatedToType.equals("candidate")) {
            JsonPath json = function.createNewCandidateWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            entitySlug = json.get("slug");
        }
        if (relatedToType.equals("company")) {
            JsonPath json = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            entitySlug = json.get("slug");
        }
        if (relatedToType.equals("contact")) {
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.get("slug");
            JsonPath json = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            entitySlug = json.get("slug");
        }
        if (relatedToType.equals("deal")) {
            JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            entitySlug = jsonDeal.get("slug");
        }
        if (relatedToType.equals("job")) {
            JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, apiAuthToken).jsonPath();
            String companySlug = jsonCompany.get("slug");
            JsonPath jsonContact = function.createNewContact_POST(baseURL, apiAuthToken, companySlug).jsonPath();
            String contactSlug = jsonContact.get("slug");
            JsonPath json = function.createNewJob(baseURL, apiAuthToken, companySlug, contactSlug).jsonPath();
            entitySlug = json.get("slug");
        }
        return entitySlug;
    }

    public int getEntityId(String entityType, String entitySlug, String albatrossAuthToken) {
        int entityId = 0;
        if (entityType.equals("candidate")) {
            entityId = privateFunction.getCandidateResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.candidate.id");
        }
        if (entityType.equals("company")) {
            entityId = privateFunction.getCompanyResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.company.id");
        }
        if (entityType.equals("contact")) {
            entityId = Integer.parseInt(privateFunction.getContactResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.contact.id"));
        }
        if (entityType.equals("deal")) {
            entityId = privateFunction.getDealResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.deal.id");
        }
        if (entityType.equals("job")) {
            entityId = privateFunction.getJobResponse(albatrossURL, albatrossAuthToken, entitySlug).jsonPath().get("data.job.id");
        }
        return entityId;
    }
}

