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
public class TimesheetDealSearchEntityTest extends DealFilterTestSupport {

    @BeforeClass
    public void setUp() {
        ensureDealFilterTestData();
    }

    @Test(dataProvider = "timesheetDealSearchEntityData")
    public void timesheetDealSearchEntityTest(String testId, String searchTerm, Integer expectedDealId,
                                             String expectedDealName, String expectedStageName,
                                             Integer expectedOwnerUserId, boolean shouldContain) {
        Response response = searchDealFilterEntities(searchTerm);

        assertThat(testId + ": Response code should be 200", response.getStatusCode(), equalTo(200));
        assertThat(testId + ": Message should be correct", response.jsonPath().get("meta.message"),
                equalTo("Entities searched successfully"));

        List<Map<String, Object>> deals = getDealsFromSearchEntity(response);

        if (shouldContain) {
            assertThat(testId + ": Deals list should not be empty", deals.isEmpty(), is(false));
            boolean matched = deals.stream().anyMatch(deal -> matchesExpectedDeal(
                    deal, expectedDealId, expectedDealName, expectedStageName, expectedOwnerUserId));
            assertThat(testId + ": Expected deal should be present in search results", matched, is(true));
        } else if (expectedDealId != null) {
            boolean matched = deals.stream().anyMatch(deal ->
                    expectedDealId.equals(asInt(deal.get("id"))));
            assertThat(testId + ": Deal should not be present in search results", matched, is(false));
        } else {
            assertThat(testId + ": Deals list should be empty for non-matching search", deals.isEmpty(), is(true));
        }
    }

    @DataProvider(name = "timesheetDealSearchEntityData", parallel = true)
    public Object[][] timesheetDealSearchEntityDataProvider() {
        ensureDealFilterTestData();
        DealFilterTestContext ctx = dealFilterContext;
        String nonExistentDealSearch = getNonExistentSearchLabel("NonExistentDeal");
        String nonExistentDealIdSearch = String.valueOf(getNonExistentEntityId());

        return new Object[][] {
                {"DS001", "FilterDeal_Alpha", ctx.dealA.id, ctx.dealA.name, ctx.dealA.stageName,
                        ctx.dealA.ownerUserId, true},
                {"DS002", ctx.dealB.name, ctx.dealB.id, ctx.dealB.name, ctx.dealB.stageName,
                        ctx.dealB.ownerUserId, true},
                {"DS003", String.valueOf(ctx.dealA.srno), ctx.dealA.id, ctx.dealA.name, ctx.dealA.stageName,
                        ctx.dealA.ownerUserId, true},
                {"DS004", ctx.dealA.stageName, ctx.dealA.id, ctx.dealA.name, ctx.dealA.stageName,
                        ctx.dealA.ownerUserId, true},
                {"DS005", ctx.dealB.stageName, ctx.dealB.id, ctx.dealB.name, ctx.dealB.stageName,
                        ctx.dealB.ownerUserId, true},
                {"DS006", ctx.dealA.ownerName, ctx.dealA.id, ctx.dealA.name, ctx.dealA.stageName,
                        ctx.dealA.ownerUserId, true},
                {"DS009", firstSearchToken(ctx.dealA.ownerName), ctx.dealA.id, ctx.dealA.name, ctx.dealA.stageName,
                        ctx.dealA.ownerUserId, true},
                {"DS007", nonExistentDealSearch, null, null, null, null, false},
                {"DS008", nonExistentDealIdSearch, null, null, null, null, false}
        };
    }

    private boolean matchesExpectedDeal(Map<String, Object> deal, Integer expectedDealId,
                                        String expectedDealName, String expectedStageName,
                                        Integer expectedOwnerUserId) {
        Integer id = asInt(deal.get("id"));
        String title = stringValue(deal.get("title"));
        String stageName = stringValue(deal.get("stagename"));
        Integer ownerId = asInt(deal.get("owner"));

        if (expectedDealId != null && !expectedDealId.equals(id)) {
            return false;
        }
        if (expectedDealName != null && !expectedDealName.equals(title)) {
            return false;
        }
        if (expectedStageName != null && !expectedStageName.isEmpty() && !expectedStageName.equals(stageName)) {
            return false;
        }
        if (expectedOwnerUserId != null && !expectedOwnerUserId.equals(ownerId)) {
            return false;
        }
        if (stringValue(deal.get("slug")).isEmpty()) {
            return false;
        }
        return "11".equals(String.valueOf(deal.get("entitytype")));
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : "";
    }

    private Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }
}
