package io.rcrm.api.listeners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.qa.api.util.reaper.ThreadManager;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.internal.TestResult;
import org.testng.xml.XmlSuite;

import io.rcrm.api.pojo.reaper.Account;
import io.rcrm.api.restclient.CurlCaptureFilter;
import io.rcrm.api.restclient.LastRequestCurl;
import io.rcrm.api.restclient.LastRequestResponse;
import io.rcrm.api.testbase.TestBase.AccountType;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReporterNG implements IReporter, ITestListener {

	private static final Path REPORT_DIR = ExtentSparkUi.surefireReportDir();
	private static final String ITEST_ATTR_LOGIN_EMAIL = "extent.login.email";

	static {
		CurlCaptureFilter.register();
	}

	private ExtentReports extent;
	private final ContractStaffingReporter contractStaffingReporter = new ContractStaffingReporter();

	@Override
	public void onStart(ITestContext context) {
	}

	@Override
	public void onFinish(ITestContext context) {
	}

	@Override
	public void onTestStart(ITestResult result) {
		LastRequestCurl.clear();
		LastRequestResponse.clear();
		attachLoginEmailAttribute(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
	}

	@Override
	public void onTestFailure(ITestResult result) {
		attachLastHttpAttributes(result);
		attachLoginEmailAttribute(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		attachLastHttpAttributes(result);
		attachLoginEmailAttribute(result);
	}

	/** Stash last curl + response on result for HTML report (fail or skip). */
	private static void attachLastHttpAttributes(ITestResult result) {
		String curl = LastRequestCurl.get();
		if (curl != null && !curl.isEmpty()) {
			result.setAttribute(LastRequestCurl.ITEST_ATTR_LAST_CURL, curl);
		}
		String responseSample = LastRequestResponse.get();
		if (responseSample != null && !responseSample.isEmpty()) {
			result.setAttribute(LastRequestResponse.ITEST_ATTR_LAST_RESPONSE, responseSample);
		}
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	@Override
	@SuppressWarnings("unused")
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
		try {
			Files.createDirectories(REPORT_DIR);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Ignore TestNG outputDirectory so report always lives next to Surefire output.
		ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_DIR.resolve("ExtentReport.html").toString());
		spark.config().setDocumentTitle("ExtentReport");
		spark.config().setReportName("ExtentReport");
		ExtentSparkUi.configureSpark(spark);

		extent = new ExtentReports();
		extent.attachReporter(spark);

		Map<String, ConsolidatedResult> consolidated = new HashMap<>();
		for (ISuite suite : suites) {
			Map<String, ISuiteResult> results = suite.getResults();
			for (ISuiteResult result : results.values()) {
				ITestContext context = result.getTestContext();
				collectResults(consolidated, context.getPassedTests(), Status.PASS);
				collectResults(consolidated, context.getFailedTests(), Status.FAIL);
				collectResults(consolidated, context.getSkippedTests(), Status.SKIP);
			}
		}
		splitMismergedDataProviderBuckets(consolidated);
		buildConsolidatedTestNodes(consolidated);

		long passed = consolidated.values().stream().filter(c -> c.finalStatus == Status.PASS).count();
		long failed = consolidated.values().stream().filter(c -> c.finalStatus == Status.FAIL).count();
		long skipped = consolidated.values().stream().filter(c -> c.finalStatus == Status.SKIP).count();
		long minStart = Long.MAX_VALUE;
		long maxEnd = Long.MIN_VALUE;
		for (ISuite suite : suites) {
			for (ISuiteResult r : suite.getResults().values()) {
				ITestContext ctx = r.getTestContext();
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
		ExtentSparkUi.applyDashboardMetricsFooter(spark, passed, failed, skipped, durationMs);
		extent.flush();
	}

	private void collectResults(Map<String, ConsolidatedResult> consolidated, IResultMap tests, Status status) {
		if (tests.size() == 0) {
			return;
		}
		for (ITestResult result : tests.getAllResults()) {
			String key = getResultKey(result);
			ConsolidatedResult bucket = consolidated.computeIfAbsent(key, k -> new ConsolidatedResult(result));
			bucket.addAttempt(result, status);
		}
	}

	/**
	 * When many data-provider invocations share one {@link #consolidationKey(ITestResult)} (parallel DP /
	 * index quirks), they land in one bucket and look like retries ("Attempt N of M"). Split by real row
	 * signature; if one signature but &gt; 3 executions, treat as collapsed DP rows and emit one node each.
	 */
	private static void splitMismergedDataProviderBuckets(Map<String, ConsolidatedResult> consolidated) {
		Map<String, ConsolidatedResult> out = new LinkedHashMap<>();
		int seq = 0;
		for (Map.Entry<String, ConsolidatedResult> e : new ArrayList<>(consolidated.entrySet())) {
			String baseKey = e.getKey();
			ConsolidatedResult c = e.getValue();
			if (c.attempts.size() <= 1) {
				out.put(baseKey, c);
				continue;
			}
			Map<String, List<Attempt>> bySig = new LinkedHashMap<>();
			for (Attempt a : c.attempts) {
				String sig = dataRowSignature(a.result);
				bySig.computeIfAbsent(sig, k -> new ArrayList<>()).add(a);
			}
			if (bySig.size() > 1) {
				for (List<Attempt> group : bySig.values()) {
					out.put(baseKey + "~dp" + (seq++), mergeAttemptsIntoConsolidated(group));
				}
			} else if (c.attempts.size() > 3) {
				for (Attempt a : c.attempts) {
					out.put(baseKey + "~dp" + (seq++), mergeAttemptsIntoConsolidated(Collections.singletonList(a)));
				}
			} else {
				out.put(baseKey, c);
			}
		}
		consolidated.clear();
		consolidated.putAll(out);
	}

	private static ConsolidatedResult mergeAttemptsIntoConsolidated(List<Attempt> attempts) {
		ConsolidatedResult c = new ConsolidatedResult(attempts.get(0).result);
		for (Attempt a : attempts) {
			c.addAttempt(a.result, a.status);
		}
		return c;
	}

	/** Row identity for split: TestNG index + param hash + string form of args (avoids hash collisions). */
	private static String dataRowSignature(ITestResult r) {
		StringBuilder sb = new StringBuilder();
		sb.append(dataProviderRowIndex(r)).append('|').append(parametersContentHash(r)).append('|');
		Object[] p = r.getParameters();
		if (p != null) {
			for (Object o : p) {
				sb.append(o == null ? "null" : o.toString()).append('\u001e');
			}
		}
		return sb.toString();
	}

	/**
	 * One Extent node per logical test (class#method#instance#data-provider row). Retries for the same row
	 * merge; each data row is its own node.
	 */
	private void buildConsolidatedTestNodes(Map<String, ConsolidatedResult> consolidated) {
		List<ConsolidatedResult> ordered = new ArrayList<>(consolidated.values());
		ordered.sort(Comparator.comparingLong(c -> c.startMillis));

		for (ConsolidatedResult c : ordered) {
			ExtentTest test = extent.createTest(c.name);
			test.getModel().setStartTime(getTime(c.startMillis));
			test.getModel().setEndTime(getTime(c.endMillis));

			for (String group : c.groups) {
				test.assignCategory(group);
			}

			if (contractStaffingReporter.isContractStaffingTest(c.referenceResult) &&
					(c.finalStatus == Status.FAIL || c.finalStatus == Status.SKIP)) {
				contractStaffingReporter.addContractStaffingTestDetails(test, c.referenceResult, c.finalStatus);
			}
			String loginEmail = resolveLoginEmailFromAttempts(c.attempts);
			test.log(Status.INFO, "Login email: " + (loginEmail == null ? "n/a" : loginEmail));
			ITestResult filterSearchSource = pickResultForFilterSearchLogs(c.attempts);
			if (filterSearchSource != null) {
				attachFilterSearchLogs(test, filterSearchSource);
			}

			c.attempts.sort(Comparator.comparingLong(a -> a.result.getStartMillis()));
			int totalAttempts = c.attempts.size();
			String firstExecutionFingerprint = fingerprintForFirstAttempt(c.attempts);
			boolean convertRetrySkipsToFail = c.finalStatus != Status.SKIP;
			for (int i = 0; i < c.attempts.size(); i++) {
				Attempt attempt = c.attempts.get(i);
				Status attemptStatus = (convertRetrySkipsToFail && attempt.status == Status.SKIP)
						? Status.FAIL
						: attempt.status;
				int attemptNumber = i + 1;
				String prefix = "Attempt " + attemptNumber + " (" + attemptStatus + ")";
				Throwable t = attempt.result.getThrowable();
				if (t != null) {
					String fp = getThrowableFingerprint(t);
					if (attemptNumber > 1 && firstExecutionFingerprint != null
							&& firstExecutionFingerprint.equals(fp)) {
						test.log(attemptStatus, sameFailureAsFirstExecutionLine(attemptNumber, totalAttempts));
					} else {
						test.log(attemptStatus,
								attemptWithThrowableContext(attemptStatus, attemptNumber, totalAttempts, attempt.result));
						test.log(attemptStatus, t);
					}
				} else {
					test.log(attemptStatus, prefix + " passed");
				}
			}
			ITestResult httpSource = pickResultForSingleHttpCapture(c.attempts);
			if (httpSource != null) {
				attachHttpDetails(test, httpSource);
			}
			test.getModel().setStatus(c.finalStatus);
		}
	}

	private static void attachLoginEmailAttribute(ITestResult result) {
		if (result.getAttribute(ITEST_ATTR_LOGIN_EMAIL) != null) {
			return;
		}
		String loginEmail = resolveLoginEmail(result);
		if (loginEmail != null && !loginEmail.trim().isEmpty()) {
			result.setAttribute(ITEST_ATTR_LOGIN_EMAIL, loginEmail);
		}
	}

	private static String resolveLoginEmailFromAttempts(List<Attempt> attempts) {
		for (Attempt attempt : attempts) {
			Object attr = attempt.result.getAttribute(ITEST_ATTR_LOGIN_EMAIL);
			if (attr instanceof String && !((String) attr).trim().isEmpty()) {
				return ((String) attr).trim();
			}
		}
		return null;
	}

	private static String resolveLoginEmail(ITestResult result) {
		try {
			Account account = ThreadManager.getAccount();
			if (account == null || account.getOwner() == null) {
				return null;
			}

			AccountType accountTypeMethod = result.getMethod().getConstructorOrMethod().getMethod()
					.getAnnotation(AccountType.class);
			AccountType accountTypeClass = result.getTestClass().getRealClass().getAnnotation(AccountType.class);
			String accountTypeValue = accountTypeMethod != null ? accountTypeMethod.value()
					: (accountTypeClass != null ? accountTypeClass.value() : "");
			String normalized = accountTypeValue == null ? "" : accountTypeValue.toLowerCase();

			if (normalized.contains("email2")) {
				String connected2 = account.getOwner().getConnectedEmail_2();
				if (connected2 != null && !connected2.trim().isEmpty()) {
					return connected2.trim();
				}
			}
			if (normalized.contains("email1") || normalized.contains("emailconnected") || normalized.contains("email")) {
				String connected1 = account.getOwner().getConnectedEmail_1();
				if (connected1 != null && !connected1.trim().isEmpty()) {
					return connected1.trim();
				}
			}
			String ownerEmail = account.getOwner().getEmail();
			if (ownerEmail != null && !ownerEmail.trim().isEmpty()) {
				return ownerEmail.trim();
			}
		} catch (Exception ignored) {
			// Best-effort enrichment for reporting only.
		}
		return null;
	}

	/**
	 * Head line for an attempt with a throwable (Automate-Selenium style: Script failed / Test skipped at method).
	 * When {@code totalAttempts} &gt; 1, prefixes {@code Attempt N of M — } so retries are unambiguous in the report.
	 */
	private static String attemptWithThrowableContext(Status attemptStatus, int attemptNumber, int totalAttempts,
	                                                  ITestResult result) {
		String atMethod = testAtMethodLine(result);
		String core = (attemptStatus == Status.SKIP) ? "Test skipped at method: " + atMethod
				: "Script failed at method: " + atMethod;
		if (totalAttempts > 1) {
			return "Attempt " + attemptNumber + " of " + totalAttempts + " — " + core;
		}
		return core;
	}

	private static String testAtMethodLine(ITestResult result) {
		return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
	}

	/** Fingerprint of Attempt 1's throwable, or {@code null} if first attempt had no throwable. */
	private static String fingerprintForFirstAttempt(List<Attempt> attempts) {
		if (attempts.isEmpty()) {
			return null;
		}
		Throwable t = attempts.get(0).result.getThrowable();
		return t == null ? null : getThrowableFingerprint(t);
	}

	private static String sameFailureAsFirstExecutionLine(int attemptNumber, int totalAttempts) {
		if (totalAttempts > 1) {
			return "Attempt " + attemptNumber + " of " + totalAttempts
					+ " — same failure as first execution (Attempt 1); stack trace not repeated.";
		}
		return "Same failure as first execution (Attempt 1); stack trace not repeated.";
	}

	/**
	 * One curl/response block for the whole test: prefer the latest attempt that stashed HTTP on the result.
	 */
	private static ITestResult pickResultForSingleHttpCapture(List<Attempt> attempts) {
		for (int i = attempts.size() - 1; i >= 0; i--) {
			ITestResult r = attempts.get(i).result;
			Object curlAttr = r.getAttribute(LastRequestCurl.ITEST_ATTR_LAST_CURL);
			if (curlAttr instanceof String && !((String) curlAttr).isEmpty()) {
				return r;
			}
		}
		return null;
	}

	private static String getThrowableFingerprint(Throwable throwable) {
		String message = throwable.getMessage();
		String truncMessage = (message != null && message.length() > 200) ? message.substring(0, 200) : message;
		return throwable.getClass().getName() + "|" + truncMessage;
	}

	private static ITestResult pickResultForFilterSearchLogs(List<Attempt> attempts) {
		for (int i = attempts.size() - 1; i >= 0; i--) {
			ITestResult r = attempts.get(i).result;
			Object logs = r.getAttribute(FilterSearchReporter.ITEST_ATTR_FILTER_SEARCH_LOGS);
			if (logs instanceof List && !((List<?>) logs).isEmpty()) {
				return r;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static void attachFilterSearchLogs(ExtentTest test, ITestResult result) {
		Object raw = result.getAttribute(FilterSearchReporter.ITEST_ATTR_FILTER_SEARCH_LOGS);
		if (!(raw instanceof List)) {
			return;
		}
		List<?> values = (List<?>) raw;
		if (values.isEmpty()) {
			return;
		}
		List<String> safeLogs = new ArrayList<>();
		for (Object value : values) {
			if (value instanceof String && !((String) value).isEmpty()) {
				safeLogs.add((String) value);
			}
		}
		for (String line : safeLogs) {
			test.log(Status.INFO, line);
		}
	}

	private void attachHttpDetails(ExtentTest test, ITestResult result) {
		Object curlAttr = result.getAttribute(LastRequestCurl.ITEST_ATTR_LAST_CURL);
		if (curlAttr instanceof String && !((String) curlAttr).isEmpty()) {
			Object respAttr = result.getAttribute(LastRequestResponse.ITEST_ATTR_LAST_RESPONSE);
			String resp = respAttr instanceof String ? (String) respAttr : null;
			String curl = (String) curlAttr;
			String curlBlock = ExtentSparkUi.collapsiblePre("Last HTTP request (curl)", curl, true);
			if (curlBlock != null) {
				test.log(Status.INFO, curlBlock);
			}
			if (resp != null && !resp.isEmpty()) {
				String respBlock = ExtentSparkUi.collapsiblePre("Last HTTP response (sample)", resp);
				if (respBlock != null) {
					test.log(Status.INFO, respBlock);
				}
			}
		}
	}

	private String getResultKey(ITestResult result) {
		return consolidationKey(result);
	}

	/**
	 * Extent merge key: one node per logical invocation. Failed reruns (same DP row) and data-driven rows
	 * can coexist: different rows differ by {@link TestResult#getParameterIndex()} and/or
	 * {@link Arrays#deepHashCode(Object[])} of parameters; a rerun keeps the same index and usually the
	 * same deep hash when TestNG passes value-equal arguments (String, Number, etc.). Custom parameter types
	 * without stable {@code hashCode()} may get a separate Extent node per attempt.
	 */
	public static String consolidationKey(ITestResult result) {
		return result.getTestClass().getName()
				+ "#" + result.getMethod().getMethodName()
				+ "#" + System.identityHashCode(result.getInstance())
				+ "#dp" + dataProviderRowIndex(result)
				+ "#pch" + parametersContentHash(result);
	}

	/** TestNG sets this per invocation (incl. retries); non-{@link TestResult} → 0. */
	private static int dataProviderRowIndex(ITestResult result) {
		if (result instanceof TestResult) {
			return ((TestResult) result).getParameterIndex();
		}
		return 0;
	}

	private static int parametersContentHash(ITestResult result) {
		Object[] params = result.getParameters();
		if (params == null || params.length == 0) {
			return 0;
		}
		return Arrays.deepHashCode(params);
	}

	private static String buildExtentNodeName(ITestResult result) {
		String method = result.getMethod().getMethodName();
		Object[] params = result.getParameters();
		if (params != null && params.length > 0) {
			return method + " [data row " + (dataProviderRowIndex(result) + 1) + "]";
		}
		return method;
	}

	/** Spark node title; same for post-run consolidation and {@link ExtentReportListener}. */
	static String extentDisplayName(ITestResult result) {
		return buildExtentNodeName(result);
	}

	private static final class Attempt {
		private final ITestResult result;
		private final Status status;

		private Attempt(ITestResult result, Status status) {
			this.result = result;
			this.status = status;
		}
	}

	private static final class ConsolidatedResult {
		private final String name;
		private final ITestResult referenceResult;
		private final List<Attempt> attempts = new ArrayList<>();
		private final Set<String> groups = new TreeSet<>();
		private long startMillis = Long.MAX_VALUE;
		private long endMillis = Long.MIN_VALUE;
		private long finalStatusAtMillis = Long.MIN_VALUE;
		private Status finalStatus = Status.SKIP;

		private ConsolidatedResult(ITestResult result) {
			this.name = buildExtentNodeName(result);
			this.referenceResult = result;
		}

		private void addAttempt(ITestResult result, Status status) {
			attempts.add(new Attempt(result, status));
			startMillis = Math.min(startMillis, result.getStartMillis());
			endMillis = Math.max(endMillis, result.getEndMillis());
			groups.addAll(Arrays.asList(result.getMethod().getGroups()));
			finalStatusAtMillis = Math.max(finalStatusAtMillis, result.getEndMillis());
			finalStatus = pickFinalStatus(finalStatus, status);
		}

		private static Status pickFinalStatus(Status current, Status incoming) {
			if (incoming == Status.PASS) {
				return Status.PASS;
			}
			if (incoming == Status.FAIL) {
				return current == Status.PASS ? Status.PASS : Status.FAIL;
			}
			return current;
		}
	}

	private Date getTime(long millis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		return calendar.getTime();
	}

}
