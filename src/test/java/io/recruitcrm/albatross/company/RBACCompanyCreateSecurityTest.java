package io.recruitcrm.albatross.company;

import java.util.HashMap;
import java.util.Map;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.albatross.RBAC2LevelAccessDataProvider;
import io.rcrm.api.javafaker.JavaFakerCompany;
import org.hamcrest.Matchers;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.javafaker.JavaFakerContact;
import io.recruitcrm.albatross.contact.Contact;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("RBAC")
public class RBACCompanyCreateSecurityTest extends TestBase {
    private final JavaFakerCompany fakerCompany = new JavaFakerCompany();
    private Map<String, String> albatrossTknMap;
    private Map<String, Integer> userIdsMap;
    JavaFakerCompany faker = new JavaFakerCompany();
	commanFunction function = new commanFunction();
	String companyName = faker.getCompanyName();
	String companyWebsite = faker.getUrl();
	String companyCity = faker.getCity();
	String address = faker.getAddress();
	int industry_id = faker.getIndustry_id();
	String logo = faker.getLogoURL();
	String aboutCompany = faker.getCompanyAbout();
	JavaFakerContact contactFaker = new JavaFakerContact();
	String contactFirstName = contactFaker.getFirstName();
	String contactLastName = contactFaker.getLastName();
	String contactEmail = "rcrmtest0@gmail.com";
	String contactNumber = contactFaker.getContactNumber();

    private static final String SUCCESS_MESSAGE = "Success";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";
    private static final String ACCESS_DENIED_MESSAGE = "Failed to Add Company : Access Denied";

    @BeforeClass(alwaysRun = true)    public void setupToken() {
        albatrossTknMap = new HashMap<>();
        userIdsMap = new HashMap<>();
        setupRbacTokensAndUserIds(albatrossTknMap, userIdsMap);
    }

    private Response createCompany(String token) {
        Company company = new Company();
		company.setCompanyname(companyName);
		company.setWebsite(companyWebsite);
		company.setAboutcompany(companyName+"\n"+ companyWebsite +"\n"+ address);
		company.setCity(companyCity);
		company.setIndustryid(industry_id);
		company.setAddress(address);

		Contact contact = new Contact();
		contact.setFirstname(contactFirstName);
		contact.setLastname(contactLastName);
		contact.setContactnumber(contactNumber);
		contact.setEmail(contactEmail);
		contact.setStageid("1");

		CompanyJson companyJson = new CompanyJson();
		companyJson.setAddress_changed(true);
		companyJson.setCompany(company);
		companyJson.setContact(contact);

        String basePath = "companies";
        return RestClient.doPost("JSON", albatrossURL, basePath, token, null, true, companyJson);
    }

    
    private void validateCompanyResponse(Response response, int expectedStatusCode, String expectedMessage, String testDescription) {
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
                    response.then().body("data.company.slug", Matchers.notNullValue());
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

    @Owner("Ajendra Singh")
    @Test(dataProvider = "company2LevelCreateAccessData", groups = {"role-based", "company-2level-create-access"})
    public void createCompanySecurityTest(String role, String access, int expectedStatusCode, String expectedMessage, String testDescription) {
        String roleToken = albatrossTknMap.get(role);
        Response createResponse = createCompany(roleToken);
        validateCompanyResponse(createResponse, expectedStatusCode, expectedMessage, testDescription);
    }

    @DataProvider(name = "company2LevelCreateAccessData", parallel = true)
    public Object[][] company2LevelCreateAccessData(ITestContext context) {
        return RBAC2LevelAccessDataProvider.getCompanyAccessData(context);
    }
}
