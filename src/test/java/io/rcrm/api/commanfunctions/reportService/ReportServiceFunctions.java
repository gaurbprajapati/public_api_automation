package io.rcrm.api.commanfunctions.reportService;

import java.util.ArrayList;
import com.qa.api.util.reaper.ThreadManager;
import org.testng.Assert;

import com.google.gson.Gson;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.javafaker.JavaFakerReport;
import io.rcrm.api.javafaker.albatross.report.*;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.SavePerference.*;
import io.recruitcrm.report.pojo.ScheduleReport.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class ReportServiceFunctions extends TestBase {

	ListFunctions listFunctions = new ListFunctions();
	JavaFakerReport javaFakerReport = new JavaFakerReport();
	JavaFakerScheduleReport scheduleReportFaker = new JavaFakerScheduleReport();

	public ReportServiceFunctions() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArrayList<Object> getAllKPILists() {
		ArrayList<Object> kpiLists = new ArrayList<Object>();

		KpiLists KpiLists3 = new KpiLists();
		KpiLists3.setLabel("Candidates Added");
		KpiLists3.setValue("cadded");
		KpiLists3.setChecked(true);

		KpiLists kpiLists5 = new KpiLists();
		kpiLists5.setLabel("Billable Revenue");
		kpiLists5.setValue("billable");
		kpiLists5.setChecked(true);

		KpiLists kpiLists6 = new KpiLists();
		kpiLists6.setLabel("Cash Collected");
		kpiLists6.setValue("cash");
		kpiLists6.setChecked(true);

		KpiLists kpiLists7 = new KpiLists();
		kpiLists7.setLabel("Sent Email Conversations");
		kpiLists7.setValue("email");
		kpiLists7.setChecked(true);

		KpiLists KpiLists1 = new KpiLists();
		KpiLists1.setLabel("Tasks Added");
		KpiLists1.setValue("task");
		KpiLists1.setChecked(true);

		KpiLists KpiLists2 = new KpiLists();
		KpiLists2.setLabel("Total Meetings Added");
		KpiLists2.setValue("appointment");
		KpiLists2.setChecked(true);

		KpiLists kpiLists8 = new KpiLists();
		kpiLists8.setLabel("Scheduled Meetings");
		kpiLists8.setValue("appointmentscheduled");
		kpiLists8.setChecked(true);

		KpiLists kpiLists9 = new KpiLists();
		kpiLists9.setLabel("Contact Meetings Added");
		kpiLists9.setValue("appointmentcontact");
		kpiLists9.setChecked(true);

		KpiLists kpiLists14 = new KpiLists();
		kpiLists14.setLabel("Canddiate Meetings Added");
		kpiLists14.setValue("appointmentcandidate");
		kpiLists14.setChecked(true);
		
		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Jobs Added");
		KpiLists4.setValue("job");
		KpiLists4.setChecked(true);

		KpiLists kpiLists10 = new KpiLists();
		kpiLists10.setLabel("Companies Added");
		kpiLists10.setValue("company");
		kpiLists10.setChecked(true);

		KpiLists kpiLists11 = new KpiLists();
		kpiLists11.setLabel("Contacts Added");
		kpiLists11.setValue("contact");
		kpiLists11.setChecked(true);

		KpiLists kpiLists12 = new KpiLists();
		kpiLists12.setLabel("Hiring Stage - Assigned");
		kpiLists12.setValue("1");
		kpiLists12.setChecked(true);

		KpiLists kpiLists13 = new KpiLists();
		kpiLists13.setLabel("Hiring Stage - Placed");
		kpiLists13.setValue("8");
		kpiLists13.setChecked(true);

		kpiLists.add(KpiLists1);
		kpiLists.add(KpiLists2);
		kpiLists.add(KpiLists3);
		kpiLists.add(KpiLists4);

		kpiLists.add(kpiLists5);
		kpiLists.add(kpiLists6);
		kpiLists.add(kpiLists7);
		kpiLists.add(kpiLists8);

		kpiLists.add(kpiLists9);
		kpiLists.add(kpiLists10);
		kpiLists.add(kpiLists11);
		kpiLists.add(kpiLists12);

		kpiLists.add(kpiLists13);
		kpiLists.add(kpiLists14);
		

		return kpiLists;

		// kpi_lists: "[\"value\":\"8\",\"label\":\"Hiring Stage -
		// Placed\",\"checked\":true},{\"value\":\"calllogs\",\"label\":\"Total Call
		// Logs
		// Added\",\"checked\":true},{\"value\":\"calllogscandidate\",\"label\":\"Candidate
		// Call Logs
		// Added\",\"checked\":true},{\"value\":\"calllogscontact\",\"label\":\"Contact
		// Call Logs
		// Added\",\"checked\":true},{\"value\":\"incomingcalllogs\",\"label\":\"Incoming
		// Call Logs
		// Added\",\"checked\":true},{\"value\":\"outgoingcalllogs\",\"label\":\"Outgoing
		// Call Logs
		// Added\",\"checked\":true},{\"value\":\"calltype_195\",\"label\":\"Call Type -
		// Call Type
		// cIpI\",\"checked\":true},{\"value\":\"calltype_197\",\"label\":\"Call Type -
		// Note Type
		// aVpy\",\"checked\":true},{\"value\":\"calltype_198\",\"label\":\"Call Type -
		// Call Type 2\",\"checked\":true},{\"value\":\"notes\",\"label\":\"Notes
		// Added\",\"checked\":true},{\"value\":\"notetype_5\",\"label\":\"Note Type -
		// Call\",\"checked\":true},{\"value\":\"notetype_4\",\"label\":\"Note Type - To
		// Do\",\"checked\":true},{\"value\":\"candidate_pitched_1\",\"label\":\"Candidate
		// Pitched -
		// Pitched\",\"checked\":true},{\"value\":\"candidate_pitched_72\",\"label\":\"Candidate
		// Pitched - Pitch cand cIpI\",\"checked\":true}]"

	}

	public String createReportPreference(String baseURL, String reportServiceURL, String token, String reportName) {
		ArrayList<Integer> teamIds = new ArrayList<>();
        JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        ArrayList<Integer> recruiterIds = jsonGetCollabrators.get("id");

        ArrayList<Object> kpiLists = new ArrayList<>();

        KpiLists kpiList1 = new KpiLists();
        kpiList1.setLabel(javaFakerReport.getRandomWord());
        kpiList1.setValue(javaFakerReport.getRandomWord());
        kpiList1.setChecked(true);

        KpiLists kpiList2 = new KpiLists();
        kpiList2.setLabel(javaFakerReport.getRandomWord());
        kpiList2.setValue(javaFakerReport.getRandomWord());
        kpiList2.setChecked(true);

        kpiLists.add(kpiList1);
        kpiLists.add(kpiList2);

        Gson gson = new Gson();
        String kpiListStringJson = gson.toJson(kpiLists);

        Settings settings = new Settings();
        settings.setRecruiter_ids(recruiterIds);
        settings.setTeam_ids(teamIds);
        settings.setKpi_lists(kpiListStringJson);
        settings.setFrom_date(String.valueOf(javaFakerReport.getFromDate()));
        settings.setTo_date(String.valueOf(javaFakerReport.getToDate()));
        settings.setDate_format(javaFakerReport.getRandomWord());

        SavePerference savePerference = new SavePerference();
        savePerference.setName(reportName);
        savePerference.setReport_type(1);
        savePerference.setSettings(settings);

        Response response = RestClient.doPost("JSON", reportServiceURL, "reports-preferences/save", token, null, true, savePerference);
        Assert.assertEquals(response.getStatusCode(), 200);
        JsonPath jp = response.jsonPath();
        int ID = jp.get("data.id");
        return Integer.toString(ID);
    }

	public ScheduleReportRequest scheduleReportRequest(String body, ArrayList<Integer> collaboratorTeamIds, boolean isReportIdValid, String reportID) {
        ScheduleReportDetails scheduleReportDetails = ScheduleReportDetails.builder()
            .report_name(scheduleReportFaker.getScheduleReportName())
            .selectedIntervalLabel(scheduleReportFaker.getIntervalLabel())
            .date_time(scheduleReportFaker.getFutureDateTime())
            .selectedEndAfterLabel(scheduleReportFaker.getEndAfterLabel())
            .selectedEndAfterType(scheduleReportFaker.getEndAfterType())
            .selectedRepetitions(scheduleReportFaker.getSelectedRepetitions())
            .end_date(null)
            .selectedFileTypeLabel(scheduleReportFaker.getFileTypeLabel())
            .subject(scheduleReportFaker.getEmailSubject())
            .body(body)
            .build();
            

        ArrayList<Integer> collaboratorUserIds = new ArrayList<>();
        ScheduleReportRequest scheduleReportRequest = ScheduleReportRequest.builder()
            .schedule_report(scheduleReportDetails)
            .collaborator_team_ids(collaboratorTeamIds)
            .collaborator_user_ids(collaboratorUserIds)
            .reportId(isReportIdValid ? Integer.parseInt(reportID) : 99999)
            .build();

        return scheduleReportRequest;
    }

	public ArrayList<String> createMultipleScheduleReports(int count, String reportID, String reportServiceURL, String token) {
		ArrayList<String> scheduleReportIDs = new ArrayList<>();
        JsonPath jsonGetCollabrators = listFunctions.getAllCollabrators(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        ArrayList<Integer> collaboratorTeamIds = jsonGetCollabrators.get("id");
        
        for (int i = 0; i < count; i++) {
            String body = scheduleReportFaker.getEmailBody() + " " + (i + 1);
            ScheduleReportRequest scheduleReportRequest = scheduleReportRequest(body, collaboratorTeamIds, true, reportID);
            Response response = RestClient.doPost("JSON", reportServiceURL, "schedule-report", token, null, true, scheduleReportRequest);

            Assert.assertEquals(response.getStatusCode(), 200);
            JsonPath jp = response.jsonPath();
            int scheduleID = jp.get("data.id");
            scheduleReportIDs.add(Integer.toString(scheduleID));
        }
        return scheduleReportIDs;
    }
}
