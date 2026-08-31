package io.recruitcrm.scenariq.feedback;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;

import com.qa.api.util.Owner;

import io.rcrm.api.pojo.scenariq.FeedbackRequest;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.scenariq.ScenariqBaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

@TestBase.AccountType("NotRequired")
public class SubmitFeedbackTest extends ScenariqBaseTest {

    private String token;

    @BeforeClass
    public void setup() {
        token = setupScenariqAccount();
    }

    // ── Happy path ───────────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithValidDataTest() {
        FeedbackRequest request = FeedbackRequest.builder()
                .rating(5)
                .message(scenariqFaker.getFeedbackMessage())
                .build();

        Response response = submitFeedback(token, request);

        assertThat("Submit feedback with valid data should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Feedback id should be present in response",
                jsonPath.get("id"), notNullValue());
    }

    // ── DataProvider for all ratings ─────────────────────────────────────

    @DataProvider(name = "allRatings")
    public Object[][] allRatings() {
        return new Object[][] {
                {5, "EXCELLENT"},
                {4, "GOOD"},
                {3, "NEUTRAL"},
                {2, "POOR"},
                {1, "VERY_POOR"}
        };
    }

    @Owner("Gaurav Prajapati")
    @Test(dataProvider = "allRatings", groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithAllRatingsTest(int rating, String label) {
        FeedbackRequest request = FeedbackRequest.builder()
                .rating(rating)
                .message(scenariqFaker.getFeedbackMessage())
                .build();

        Response response = submitFeedback(token, request);

        assertThat("Submit feedback with rating " + label + " (" + rating + ") should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Feedback id should be present for rating " + label,
                jsonPath.get("id"), notNullValue());
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithLongMessageTest() {
        // Build a message longer than 4000 characters
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            longMessage.append("Feedback ");
        }
        String message = longMessage.toString();
        assertThat("Prerequisite: message should be longer than 4000 chars",
                message.length(), greaterThan(4000));

        FeedbackRequest request = FeedbackRequest.builder()
                .rating(4)
                .message(message)
                .build();

        Response response = submitFeedback(token, request);

        assertThat("Submit feedback with long message should return 200",
                response.statusCode(), is(200));

        JsonPath jsonPath = response.jsonPath();
        assertThat("Feedback id should be present for long message feedback",
                jsonPath.get("id"), notNullValue());
    }

    // ── Error path tests ─────────────────────────────────────────────────

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithInvalidRatingTest() {
        FeedbackRequest request = FeedbackRequest.builder()
                .rating(99)
                .message(scenariqFaker.getFeedbackMessage())
                .build();

        Response response = submitFeedback(token, request);

        assertThat("Submit feedback with invalid rating should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithMissingRatingTest() {
        FeedbackRequest request = FeedbackRequest.builder()
                .message(scenariqFaker.getFeedbackMessage())
                .build();

        Response response = submitFeedback(token, request);

        assertThat("Submit feedback with missing rating should return 400",
                response.statusCode(), is(400));
    }

    @Owner("Gaurav Prajapati")
    @Test(groups = {"scenariq", "nightly-build"})
    public void submitFeedbackWithInvalidTokenTest() {
        FeedbackRequest request = FeedbackRequest.builder()
                .rating(5)
                .message(scenariqFaker.getFeedbackMessage())
                .build();

        Response response = submitFeedback("invalid_token_abc123", request);

        assertThat("Submit feedback with invalid token should return 401",
                response.statusCode(), is(401));
    }
}
