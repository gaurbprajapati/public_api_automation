package io.rcrm.api.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import io.rcrm.api.restclient.CurlCaptureFilter;
import io.rcrm.api.restclient.LastRequestCurl;
import io.rcrm.api.restclient.LastRequestResponse;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.model.ReportStats;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportListener implements ITestListener {

	private static final Path REPORT_DIR = ExtentSparkUi.surefireReportDir();
	/** Same file name as {@link ExtentReporterNG} so every suite produces one canonical Spark report. */
	private static final String FILE_NAME = "ExtentReport.html";

	private static ExtentReports extent = init();
	private static ExtentSparkReporter sparkReporter;
	public static ThreadLocal<ExtentTest> test = new ThreadLocal<ExtentTest>();
	private static final ConcurrentHashMap<String, ExtentTest> testMap = new ConcurrentHashMap<>();

	static {
		CurlCaptureFilter.register();
	}

	private static ExtentReports init() {

		try {
			Files.createDirectories(REPORT_DIR);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR.resolve(FILE_NAME).toString());
		sparkReporter = spark;
		spark.config().setDocumentTitle("API Automation Test Results");
		spark.config().setReportName("API Automation Test Results");
		ExtentSparkUi.configureSpark(spark);

		extent = new ExtentReports();
		extent.attachReporter(spark);

		return extent;
	}

	public synchronized void onStart(ITestContext context) {
	}

	public synchronized void onFinish(ITestContext context) {
		if (sparkReporter != null) {
			long[] c = resolveCountsForPassBanner(context);
			long durationMs = 0L;
			if (context.getStartDate() != null) {
				durationMs = System.currentTimeMillis() - context.getStartDate().getTime();
			}
			ExtentSparkUi.applyDashboardMetricsFooter(sparkReporter, c[0], c[1], c[2], durationMs);
		}
		extent.flush();
		test.remove();
		// Removed testMap.clear() to ensure retries across contexts are consolidated
	}

	/** Prefer Extent parent stats when populated; else TestNG maps for this context. */
	private static long[] resolveCountsForPassBanner(ITestContext context) {
		try {
			ReportStats st = extent.getStats();
			if (st != null) {
				Map<Status, Long> p = st.getParent();
				if (p != null && st.sumStat(p) > 0) {
					return new long[] {
							p.getOrDefault(Status.PASS, 0L),
							p.getOrDefault(Status.FAIL, 0L),
							p.getOrDefault(Status.SKIP, 0L)
					};
				}
			}
		} catch (Exception ignored) {
			// fall through
		}
		return new long[] {
				context.getPassedTests().size(),
				context.getFailedTests().size(),
				context.getSkippedTests().size()
		};
	}

	public synchronized void onTestStart(ITestResult result) {
		LastRequestCurl.clear();
		LastRequestResponse.clear();
		String testKey = ExtentReporterNG.consolidationKey(result);
		
		String qualifiedName = result.getMethod().getQualifiedName();
		int last = qualifiedName.lastIndexOf(".");
		int mid = qualifiedName.substring(0, last).lastIndexOf(".");
		String className = qualifiedName.substring(mid + 1, last);
 
		ExtentTest extentTest = testMap.computeIfAbsent(testKey, k ->
				extent.createTest(ExtentReporterNG.extentDisplayName(result), result.getMethod().getDescription()));
 
		if (extentTest.getModel().getStartTime() == null) {
			extentTest.assignCategory(result.getTestContext().getSuite().getName());
			extentTest.assignCategory(className);
			// Only set start time for the first attempt
			extentTest.getModel().setStartTime(getTime(result.getStartMillis()));
		} else {
			extentTest.info("Retry attempt for this test");
		}
 
		test.set(extentTest);
	}

	public synchronized void onTestSuccess(ITestResult result) {
		ExtentTest currentTest = testMap.get(ExtentReporterNG.consolidationKey(result));
		if (currentTest != null) {
			currentTest.pass("Test passed");
			// Final status must be PASS after retry success (prior FAIL logs can exist on same node).
			currentTest.getModel().setStatus(Status.PASS);
			currentTest.getModel().setEndTime(getTime(result.getEndMillis()));
		}
	}

	public synchronized void onTestFailure(ITestResult result) {
		ExtentTest currentTest = testMap.get(ExtentReporterNG.consolidationKey(result));
		if (currentTest != null) {
			Throwable t = result.getThrowable();
			if (t != null) {
				currentTest.fail(t);
			} else {
				currentTest.fail("Test failed");
			}
			currentTest.getModel().setStatus(Status.FAIL);
			currentTest.getModel().setEndTime(getTime(result.getEndMillis()));
			logLastHttpCapture(result);
		}
	}

	@Override
	public synchronized void onTestSkipped(ITestResult result) {
		ExtentTest currentTest = testMap.get(ExtentReporterNG.consolidationKey(result));
		if (currentTest != null) {
			Throwable t = result.getThrowable();
			if (t != null) {
				currentTest.skip(t);
			} else {
				currentTest.skip("Test Skipped");
			}
			currentTest.getModel().setStatus(Status.SKIP);
			currentTest.getModel().setEndTime(getTime(result.getEndMillis()));
			logLastHttpCapture(result);
		}
	}

	/** Curl + response on result and Extent log (used for failed and skipped tests). */
	private void logLastHttpCapture(ITestResult result) {
		ExtentTest t = test.get();
		if (t == null) {
			return;
		}
		String curl = LastRequestCurl.get();
		if (curl != null && !curl.isEmpty()) {
			result.setAttribute(LastRequestCurl.ITEST_ATTR_LAST_CURL, curl);
			String block = ExtentSparkUi.collapsiblePre("Last HTTP request (curl)", curl, true);
			if (block != null) {
				t.log(Status.INFO, block);
			}
		}
		String responseSample = LastRequestResponse.get();
		if (responseSample != null && !responseSample.isEmpty()) {
			result.setAttribute(LastRequestResponse.ITEST_ATTR_LAST_RESPONSE, responseSample);
			String block = ExtentSparkUi.collapsiblePre("Last HTTP response (sample)", responseSample);
			if (block != null) {
				t.log(Status.INFO, block);
			}
		}
	}

	@Override
	public synchronized void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	private Date getTime(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

}
