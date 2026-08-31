package io.recruitcrm.ostrich;

import com.qa.api.util.DateUtil;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.ostrich.GetMeetingInCalendar;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class GetCalendarMeetingsTest extends TestBase {
    commanFunction function = new commanFunction();
    String albatrossTkn;

    @BeforeClass(alwaysRun = true)    public void setup() {
        albatrossTkn = ThreadManager.getOwnerAlbatrossToken();
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCalendarMeetings() {

        String startDate = DateUtil.today().toString();
        String endDate = DateUtil.addOneHourToDate(DateUtil.today()).toString();

        JsonPath jp = function.createNewMeetingsWithCustomDate(baseURL, ThreadManager.getAccountApiKey(), "candidate", startDate, endDate,
                "Upcoming").jsonPath();
        int meetingId = jp.getInt("id");
        int ownerId = jp.getInt("owner");
        String meetingTitle = jp.get("title");
        String meetingDescription = jp.getString("description");

        String basePath = "calendar/meetings";

        GetMeetingInCalendar getMeetingInCalendar = new GetMeetingInCalendar();
        getMeetingInCalendar.setUser_ids(String.valueOf(ownerId));
        getMeetingInCalendar.setStartDate(DateUtil.todayStartTime().getTime()/1000);
        getMeetingInCalendar.setEndDate(DateUtil.todayEndTime().getTime()/1000);

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken(),
                null,true,getMeetingInCalendar);

        Assert.assertEquals(response.getStatusCode(), 200);

        response.then().body("message_type", Matchers.containsString("is-success"));
        response.then().body("message", Matchers.containsString("Meetings fetched successfully."));
        response.then().body("data.meetings[0].id", Matchers.equalTo(meetingId));
        response.then().body("data.meetings[0].title", Matchers.equalTo(meetingTitle));
        response.then().body("data.meetings[0].description", Matchers.equalTo(meetingDescription));
    }

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void getCalendarMeetings_401() {

        String basePath = "calendar/meetings";

        GetMeetingInCalendar getMeetingInCalendar = new GetMeetingInCalendar();
        getMeetingInCalendar.setUser_ids("");
        getMeetingInCalendar.setStartDate(DateUtil.today().getTime());
        getMeetingInCalendar.setEndDate(DateUtil.addOneHourToDate(DateUtil.today()).getTime());

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, ThreadManager.getOwnerAlbatrossToken()+"12345",
                null, true, getMeetingInCalendar);

        Assert.assertEquals(response.getStatusCode(), 401);
        response.then().body("error", Matchers.containsString("Unauthorized"));
    }

    @Owner("Harika")
    @Test(dataProvider = "getUserIds", groups = "nightly-build")
    public void getCalendarMeetings_422(String userId, String message) {

        String basePath = "calendar/meetings";

        GetMeetingInCalendar getMeetingInCalendar = new GetMeetingInCalendar();
        getMeetingInCalendar.setUser_ids(userId);
        getMeetingInCalendar.setStartDate(DateUtil.today().getTime());
        getMeetingInCalendar.setEndDate(DateUtil.addOneHourToDate(DateUtil.today()).getTime());

        Response response = RestClient.doPost("JSON", ostrichURL, basePath, albatrossTkn,
                null,true,getMeetingInCalendar);

        Assert.assertEquals(response.getStatusCode(), 422);
        response.then().body("data.message", Matchers.containsString(message));
    }

    @DataProvider(parallel = true)
    public Object[][] getUserIds() {
        Object data[][] = { { " ", "The user ids field is required." }, { "abcd", "The user ids must be an integer." }};
        return data;
    }

}
