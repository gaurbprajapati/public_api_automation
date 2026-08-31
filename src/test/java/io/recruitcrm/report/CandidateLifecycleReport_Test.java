package io.recruitcrm.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.commanfunctions.publicapi.listFunctions.ListFunctions;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.recruitcrm.report.pojo.CandidateLifecycleReport;
import io.recruitcrm.report.pojo.KpiLists;
import io.recruitcrm.report.pojo.TeamPerformanceReport;
import io.restassured.response.Response;
import io.rcrm.api.testbase.TestBase.AccountType;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class CandidateLifecycleReport_Test extends TestBase{

    public CandidateLifecycleReport_Test(){
        super();
    }

    String slug = "";
	commanFunction function = new commanFunction();
	ListFunctions listFunctions = new ListFunctions();

	@Owner("Rahul Shibu")
	@Test(groups = "nightly-build")
	public void candidateLifecycleReport_Test() {
		
		// get /v1/getkpilistreport
		// get /v1/collabrator
		
		ArrayList<Object> kpiLists0 =new ArrayList<Object>();
		
		
		KpiLists KpiLists1 = new KpiLists();
		KpiLists1.setLabel("Applied");
		KpiLists1.setValue("10");
        KpiLists1.setChecked(true);
		
		KpiLists KpiLists2 = new KpiLists();
		KpiLists2.setLabel("Assigned");
		KpiLists2.setValue("1");
        KpiLists2.setChecked(true);

		KpiLists KpiLists3 = new KpiLists();
		KpiLists3.setLabel("Placed");
		KpiLists3.setValue("8");
		KpiLists3.setChecked(true);

		KpiLists KpiLists4 = new KpiLists();
		KpiLists4.setLabel("Candidate Pitched - Pitched");
		KpiLists4.setValue("candidate_pitched_1");
		KpiLists4.setChecked(true);
		
		
		kpiLists0.add(KpiLists1);
		kpiLists0.add(KpiLists2);
		kpiLists0.add(KpiLists3);
		kpiLists0.add(KpiLists4);

		
		CandidateLifecycleReport candidateReport = new CandidateLifecycleReport();
		candidateReport.setKpi_lists(kpiLists0);
		candidateReport.setFrom_date("1613400447");
		candidateReport.setTo_date("1644936462");
		
	
		Response response = RestClient.doPost("JSON", reportServiceURL, "reports/candidate-lifecycle-report", ThreadManager.getOwnerAlbatrossToken(), null, true,candidateReport);

		response.then().statusCode(200);
    }
    
}
