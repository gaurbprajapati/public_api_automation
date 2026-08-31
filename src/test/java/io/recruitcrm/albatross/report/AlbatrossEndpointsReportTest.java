package io.recruitcrm.albatross.report;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.albatross.report.ExportData;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import java.util.ArrayList;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AlbatrossEndpointsReportTest extends TestBase {

    @Owner("Sai Teja SG")
    @Test(groups = "nightly-build")
    public void exportReportData() {
        // Empty Payload to Check if the Endpoint is Excepting the Export Columns and Export Header Property in the Request Body
        ExportData exportData = new ExportData();
        exportData.setExportColumns(new ArrayList<>());
        exportData.setExportHeader(new ArrayList<>());

        String basePath = "reports/export-data";
        Response response = RestClient.doPost("JSON",albatrossURL,basePath, ThreadManager.getOwnerAlbatrossToken(),null,true,exportData);
        response.then().statusCode(200);
        response.then().body("message", Matchers.is("We are getting your files. This might take some time, you'll be notified through email when they are ready."));
        response.then().body("message_type", Matchers.is("is-success"));
    }

}
