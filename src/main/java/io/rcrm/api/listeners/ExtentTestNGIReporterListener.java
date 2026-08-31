package io.rcrm.api.listeners;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.xml.XmlSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentTestNGIReporterListener implements IReporter {
	private static final Path REPORT_DIR = ExtentSparkUi.surefireReportDir();
	private static final String FILE_NAME = "Extent.html";

	private ExtentReports extent;
	private ExtentSparkReporter sparkReporter;

	    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
	        init();
	        
	        for (ISuite suite : suites) {
	        	Map<String,ISuiteResult> result = suite.getResults();
	            
	            for (ISuiteResult r : result.values()) {
	                ITestContext context = r.getTestContext();
	                
	                buildTestNodes(context.getFailedTests(), Status.FAIL);
	                buildTestNodes(context.getSkippedTests(), Status.SKIP);
	                buildTestNodes(context.getPassedTests(), Status.PASS);
	                
	            }
	        }
	        
	        for (String s : Reporter.getOutput()) {
	            extent.addTestRunnerOutput(s);
	        }

	        long passed = 0;
	        long failed = 0;
	        long skipped = 0;
	        long minStart = Long.MAX_VALUE;
	        long maxEnd = Long.MIN_VALUE;
	        for (ISuite suite : suites) {
	        	for (ISuiteResult r : suite.getResults().values()) {
	        		ITestContext ctx = r.getTestContext();
	        		passed += ctx.getPassedTests().size();
	        		failed += ctx.getFailedTests().size();
	        		skipped += ctx.getSkippedTests().size();
	        		if (ctx.getStartDate() != null) {
	        			minStart = Math.min(minStart, ctx.getStartDate().getTime());
	        		}
	        		if (ctx.getEndDate() != null) {
	        			maxEnd = Math.max(maxEnd, ctx.getEndDate().getTime());
	        		}
	        	}
	        }
	        long durationMs = (minStart != Long.MAX_VALUE && maxEnd != Long.MIN_VALUE && maxEnd >= minStart)
	        		? maxEnd - minStart
	        		: 0L;
	        if (sparkReporter != null) {
	        	ExtentSparkUi.applyDashboardMetricsFooter(sparkReporter, passed, failed, skipped, durationMs);
	        }
	        extent.flush();
	    }
	    
	    private void init() {
	    	try {
	    		Files.createDirectories(REPORT_DIR);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR.resolve(FILE_NAME).toString());
	        sparkReporter = spark;
	        spark.config().setDocumentTitle("ExtentReports - Created by TestNG Listener");
	        spark.config().setReportName("ExtentReports - Created by TestNG Listener");
	        ExtentSparkUi.configureSpark(spark);
	        
	        extent = new ExtentReports();
	        extent.attachReporter(spark);
	    }
	    
	    private void buildTestNodes(IResultMap tests, Status status) {
	        ExtentTest test;
	        
	        if (tests.size() > 0) {
	            for (ITestResult result : tests.getAllResults()) {
	                test = extent.createTest(result.getMethod().getMethodName());
	                
	                for (String group : result.getMethod().getGroups())
	                    test.assignCategory(group);

	                if (result.getThrowable() != null) {
	                    test.log(status, result.getThrowable());
	                }
	                else {
	                    test.log(status, "Test " + status.toString().toLowerCase() + "ed");
	                }
	                
	                test.getModel().setStartTime(getTime(result.getStartMillis()));
	                test.getModel().setEndTime(getTime(result.getEndMillis()));
	            }
	        }
	    }
	    
	    private Date getTime(long millis) {
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTimeInMillis(millis);
	        return calendar.getTime();      
	    }
}
