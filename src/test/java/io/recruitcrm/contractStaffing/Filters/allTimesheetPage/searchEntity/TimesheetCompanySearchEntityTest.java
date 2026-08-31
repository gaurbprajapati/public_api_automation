package io.recruitcrm.contractStaffing.Filters.allTimesheetPage.searchEntity;

import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.*;
import io.recruitcrm.contractStaffing.Filters.allTimesheetPage.supporters.model.*;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestBase.AccountType("Business|AlbatrossTkn|contractStaffing")
public class TimesheetCompanySearchEntityTest extends CompanyFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureCompanyFilterTestData();
    }

    @Test(dataProvider = "timesheetCompanySearchEntityData")
    public void timesheetCompanySearchEntityTest(String testId, String searchTerm, Integer expectedCompanyId,
                                                 String expectedCompanyName, String expectedCity,
                                                 boolean shouldContain) {
        Response response = searchCompanyFilterEntities(searchTerm);

        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Entities searched successfully"));

        List<Map<String, Object>> companies = getCompaniesFromSearchEntity(response);

        if (shouldContain) {
            assertThat(testId + ": Companies list should not be empty", companies.isEmpty(), is(false));
            boolean matched = companies.stream().anyMatch(company -> matchesExpectedCompany(
                    company, expectedCompanyId, expectedCompanyName, expectedCity));
            assertThat(testId + ": Expected company should be present in search results", matched, is(true));
        } else if (expectedCompanyId != null) {
            boolean matched = companies.stream().anyMatch(company ->
                    expectedCompanyId.equals(asInt(company.get("id"))));
            assertThat(testId + ": Company should not be present in search results", matched, is(false));
        } else {
            assertThat(testId + ": Companies list should be empty for non-matching search", companies.isEmpty(), is(true));
        }
    }

    @DataProvider(name = "timesheetCompanySearchEntityData", parallel = true)
    public Object[][] timesheetCompanySearchEntityDataProvider() {
        ensureCompanyFilterTestData();
        CompanyFilterTestContext ctx = companyFilterContext;
        String nonExistentCompanySearch = getNonExistentSearchLabel("NonExistentCompany");
        String nonExistentCompanyIdSearch = String.valueOf(getNonExistentEntityId());

        return new Object[][] {
                {"CS001", "FilterCo_Alpha", ctx.companyA.id, ctx.companyA.name, ctx.companyA.city, true},
                {"CS002", ctx.companyB.name, ctx.companyB.id, ctx.companyB.name, ctx.companyB.city, true},
                {"CS003", String.valueOf(ctx.companyA.srno), ctx.companyA.id, ctx.companyA.name, ctx.companyA.city, true},
                {"CS004", ctx.companyA.city, ctx.companyA.id, ctx.companyA.name, ctx.companyA.city, true},
                {"CS005", ctx.companyB.city, ctx.companyB.id, ctx.companyB.name, ctx.companyB.city, true},
                {"CS006", ctx.companyC.name, ctx.companyC.id, ctx.companyC.name, ctx.companyC.city, true},
                {"CS007", nonExistentCompanySearch, null, null, null, false},
                {"CS008", nonExistentCompanyIdSearch, null, null, null, false},
                {"CS009", firstSearchToken(ctx.companyB.name), ctx.companyB.id, ctx.companyB.name, ctx.companyB.city, true}
        };
    }

    private boolean matchesExpectedCompany(Map<String, Object> company, Integer expectedCompanyId,
                                           String expectedCompanyName, String expectedCity) {
        Integer id = asInt(company.get("id"));
        String title = company.get("title") != null ? company.get("title").toString() : "";
        String city = company.get("city") != null ? company.get("city").toString() : "";

        if (expectedCompanyId != null && !expectedCompanyId.equals(id)) {
            return false;
        }
        if (expectedCompanyName != null && !expectedCompanyName.equals(title)) {
            return false;
        }
        if (expectedCity != null && !expectedCity.isEmpty() && !expectedCity.equals(city)) {
            return false;
        }
        return "3".equals(String.valueOf(company.get("entitytype")));
    }

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }
}
