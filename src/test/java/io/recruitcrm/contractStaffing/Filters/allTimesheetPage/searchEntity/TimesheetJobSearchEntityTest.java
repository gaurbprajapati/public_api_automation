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
public class TimesheetJobSearchEntityTest extends JobFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureJobFilterTestData();
    }

    @Test(dataProvider = "timesheetJobSearchEntityData")
    public void timesheetJobSearchEntityTest(String testId, String searchTerm, Integer expectedJobId,
                                             String expectedJobName, String expectedCity,
                                             boolean shouldContain) {
        Response response = searchJobFilterEntities(searchTerm);

        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Entities searched successfully"));

        List<Map<String, Object>> jobs = getJobsFromSearchEntity(response);

        if (shouldContain) {
            assertThat(testId + ": Jobs list should not be empty", jobs.isEmpty(), is(false));
            boolean matched = jobs.stream().anyMatch(job -> matchesExpectedJob(
                    job, expectedJobId, expectedJobName, expectedCity));
            assertThat(testId + ": Expected job should be present in search results", matched, is(true));
        } else if (expectedJobId != null) {
            boolean matched = jobs.stream().anyMatch(job ->
                    expectedJobId.equals(asInt(job.get("id"))));
            assertThat(testId + ": Job should not be present in search results", matched, is(false));
        } else {
            assertThat(testId + ": Jobs list should be empty for non-matching search", jobs.isEmpty(), is(true));
        }
    }

    @DataProvider(name = "timesheetJobSearchEntityData", parallel = true)
    public Object[][] timesheetJobSearchEntityDataProvider() {
        ensureJobFilterTestData();
        JobFilterTestContext ctx = jobFilterContext;
        String nonExistentJobSearch = getNonExistentSearchLabel("NonExistentJob");
        String nonExistentJobIdSearch = String.valueOf(getNonExistentEntityId());

        return new Object[][] {
                {"JS001", "FilterJob_Alpha", ctx.jobA.id, ctx.jobA.name, ctx.jobA.city, true},
                {"JS002", ctx.jobB.name, ctx.jobB.id, ctx.jobB.name, ctx.jobB.city, true},
                {"JS003", String.valueOf(ctx.jobA.srno), ctx.jobA.id, ctx.jobA.name, ctx.jobA.city, true},
                {"JS004", ctx.jobA.city, ctx.jobA.id, ctx.jobA.name, ctx.jobA.city, true},
                {"JS005", ctx.jobB.city, ctx.jobB.id, ctx.jobB.name, ctx.jobB.city, true},
                {"JS006", ctx.jobC.name, ctx.jobC.id, ctx.jobC.name, ctx.jobC.city, true},
                {"JS007", nonExistentJobSearch, null, null, null, false},
                {"JS008", nonExistentJobIdSearch, null, null, null, false},
                {"JS009", firstSearchToken(ctx.jobB.name), ctx.jobB.id, ctx.jobB.name, ctx.jobB.city, true}
        };
    }

    private boolean matchesExpectedJob(Map<String, Object> job, Integer expectedJobId,
                                       String expectedJobName, String expectedCity) {
        Integer id = asInt(job.get("id"));
        String title = job.get("title") != null ? job.get("title").toString() : "";
        String location = job.get("location") != null ? job.get("location").toString() : "";

        if (expectedJobId != null && !expectedJobId.equals(id)) {
            return false;
        }
        if (expectedJobName != null && !expectedJobName.equals(title)) {
            return false;
        }
        if (expectedCity != null && !expectedCity.isEmpty() && !expectedCity.equals(location)) {
            return false;
        }
        return "4".equals(String.valueOf(job.get("entitytype")));
    }

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }
}
