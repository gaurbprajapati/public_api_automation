package io.rcrm.api.testbase;

import com.qa.api.util.GenerateToken;
import com.qa.api.util.reaper.ReaperIntegration;
import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.pojo.reaper.Account;
import io.rcrm.api.pojo.reaper.AccountList;

import io.rcrm.api.restclient.RestClient;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import io.rcrm.api.listeners.ExtentReporterNG;

import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Listeners({
		io.rcrm.api.listeners.ExtentReporterNG.class,
		io.rcrm.api.listeners.RetryListener.class
})

public class TestBase {

	String env;
	protected static String baseURL;
	protected static String reportServiceURL;
	protected static String hiringPipelineServiceURL;
	protected static String neptuneServiceURL;
	protected static String albatrossURL;
	protected static String candidatesURL;
	protected static String auditLogURL;
	protected static String nymaURL;
	protected static String nymaURLv3;
	protected static String commURL;
	protected static String ostrichURL;
	protected static String executiveSummaryServiceURL;
	protected static String jobBoardServiceURL;
	protected static String authServiceURL;
	protected static String notificationServiceURL;
	protected static String baseURI;
	protected static String invoiceServiceURL;
	protected static String companyServiceURL;
	protected static String syncFunctionURL;
	protected static String contactServiceURL;
    protected static String jobServiceURL;
    protected static String ariesServiceURL;

	protected static String logicmelon_username;
	protected static String logicmelon_password;
	protected static String logicmelon_apikey;

	private static List<String> emailAddresses = new ArrayList<>();
	private static List<String> passwords = new ArrayList<>();
	private static Random random = new Random();

	// protected Map<String, String> authTokenMap = null;
	public TestBase() {
	}

	/**
	 * Tracks test classes that have already had class-level setup run.
	 * When running by groups from command line, @BeforeClass may not be invoked;
	 * @BeforeMethod uses this to run class setup once per class when needed.
	 */
	private static final Set<Class<?>> classesWithClassSetupRun = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Account resolved in {@link #beforeClass()} for the test class. ThreadLocal in {@link ThreadManager}
	 * is per worker thread; parallel methods need the same account re-applied on threads that did not run
	 * class setup.
	 */
	private static final class ClassAccountState {
		final Account account;
		final boolean setAlbatrossTokens;

		ClassAccountState(Account account, boolean setAlbatrossTokens) {
			this.account = account;
			this.setAlbatrossTokens = setAlbatrossTokens;
		}
	}

	private static final ConcurrentHashMap<Class<?>, ClassAccountState> CACHED_ACCOUNT_STATE_BY_TEST_CLASS =
			new ConcurrentHashMap<>();

	private void cacheClassAccountState(Account account, boolean setAlbatrossTokens) {
		CACHED_ACCOUNT_STATE_BY_TEST_CLASS.put(getClass(), new ClassAccountState(account, setAlbatrossTokens));
	}

	/**
	 * Re-populates {@link ThreadManager}'s ThreadLocal when this worker thread never ran {@link #beforeClass()}
	 * (e.g. TestNG parallel methods).
	 */
	private void restoreThreadLocalAccountIfNeeded() {
		ClassAccountState state = CACHED_ACCOUNT_STATE_BY_TEST_CLASS.get(getClass());
		if (state == null || ThreadManager.getAccount() != null) {
			return;
		}
		ThreadManager.setAccount(state.account);
		if (state.setAlbatrossTokens) {
			ThreadManager.setOwnerAlbatrossToken(state.account.getOwner().getToken());
			ThreadManager.setOwnerAuthCode(state.account.getOwner().getAuthCode());
		}
	}

	@BeforeSuite(alwaysRun = true)
	public void beforeSuite() {
		env = System.getProperty("envname"); // dev,test,test2,mark,app
		baseURL = "https://" + env + "api.recruitcrm.net/v1/";
		reportServiceURL = "https://" + env + "-report.recruitcrm.net/v1";
		albatrossURL = "https://albatross-" + env + ".recruitcrm.net/v1";
		candidatesURL = "https://" + env + "candidate.recruitcrm.net/v2/";
		auditLogURL = "https://" + env + "-audit-log.recruitcrm.net/v1/auditlogs/";
		executiveSummaryServiceURL = "https://" + env + "-executive-search-report.recruitcrm.net/v1";
		hiringPipelineServiceURL = "https://" + env + "-hiring-pipeline.recruitcrm.net/v1/";
		jobBoardServiceURL = "https://" + env + "-jobboard.recruitcrm.net/v1";
		nymaURL = "https://" + env + "nyma.recruitcrm.net/v2";
		nymaURLv3 = "https://" + env + "nyma.recruitcrm.net/v2/nylas-v3";
		neptuneServiceURL = "https://" + env + "-neptune.recruitcrm.net/v1/";
		authServiceURL = "https://" + env + "-auth.recruitcrm.net/v1/";
		notificationServiceURL = "https://" + env + "-notification.recruitcrm.net/v1/";
		ostrichURL = "https://" + env + "-ostrich.recruitcrm.net/v1/";
		baseURI = "https://" + env + ".recruitcrm.net/actions";
		commURL = "https://" + env + "comm.recruitcrm.net";
		invoiceServiceURL = "https://" + env + "invoice.recruitcrm.net/v2/";
		companyServiceURL = "https://" + env + "company.recruitcrm.net/v2/";
		syncFunctionURL = "https://" + env + "-syncfusion.recruitcrm.net/v1/";
		contactServiceURL = "https://" + env + "contact.recruitcrm.net/v2/";
        jobServiceURL = "https://" + env + "job.recruitcrm.net/v2/";
        ariesServiceURL = "https://" + env + "-aries.recruitcrm.net/v2/";
		logicmelon_username = "parthib@recruitcrm.io";
		logicmelon_password = "RecruitCRM111";
		logicmelon_apikey = "FF308D89-E1E2-4303-94D4-BB87D7A2F230";

		Account account = getAccounts("Business", "", 1)[0];
		ThreadManager.setAccount(account);
		ThreadManager.setOwnerAlbatrossToken(account.getOwner().getToken());
		ThreadManager.setOwnerAuthCode(account.getOwner().getAuthCode());
		StringBuffer publicApiTkn = new StringBuffer(ThreadManager.getAccountApiKey());
		StringBuffer albatrossTkn = new StringBuffer(ThreadManager.getAlbatrossToken("Owner"));
		StringBuffer baseUrlBuffer = new StringBuffer(baseURL);
		StringBuffer reportServiceURLBuffer = new StringBuffer(reportServiceURL);
		StringBuffer albatrossURLBuffer = new StringBuffer(albatrossURL);
		StringBuffer candidatesURLBuffer = new StringBuffer(candidatesURL);
		StringBuffer auditLogURLBuffer = new StringBuffer(auditLogURL);
		StringBuffer executiveSummaryServiceURLBuffer = new StringBuffer(executiveSummaryServiceURL);
		StringBuffer hiringPipelineServiceURLBuffer = new StringBuffer(hiringPipelineServiceURL);
		StringBuffer jobBoardServiceURLBuffer = new StringBuffer(jobBoardServiceURL);
		StringBuffer nymaURLBuffer = new StringBuffer(nymaURL);
		StringBuffer nyamURLv3Buffer = new StringBuffer(nymaURLv3);
		StringBuffer neptuneServiceURLBuffer = new StringBuffer(neptuneServiceURL);
		StringBuffer authServiceURLBuffer = new StringBuffer(authServiceURL);
		StringBuffer notificationServiceURLBuffer = new StringBuffer(notificationServiceURL);
		StringBuffer ostrichURLBuffer = new StringBuffer(ostrichURL);
		StringBuffer baseURIBuffer = new StringBuffer(baseURI);
		StringBuffer commURLBuffer = new StringBuffer(commURL);
		StringBuffer invoiceServiceURLBuffer = new StringBuffer(invoiceServiceURL);
		StringBuffer companyServiceURLBuffer = new StringBuffer(companyServiceURL);
		StringBuffer syncFunctionURLBuffer = new StringBuffer(syncFunctionURL);
		StringBuffer contactServiceURLBuffer = new StringBuffer(contactServiceURL);
        StringBuffer jobServiceURLBuffer = new StringBuffer(jobServiceURL);
        StringBuffer ariesServiceURLBuffer = new StringBuffer(ariesServiceURL);

		verifyAvailableServices(publicApiTkn, albatrossTkn, baseUrlBuffer, reportServiceURLBuffer, albatrossURLBuffer,
				candidatesURLBuffer,
				auditLogURLBuffer, executiveSummaryServiceURLBuffer, hiringPipelineServiceURLBuffer,
				jobBoardServiceURLBuffer, nymaURLBuffer, nyamURLv3Buffer, neptuneServiceURLBuffer, authServiceURLBuffer,
				notificationServiceURLBuffer, ostrichURLBuffer, baseURIBuffer, commURLBuffer, invoiceServiceURLBuffer,
				companyServiceURLBuffer, syncFunctionURLBuffer, contactServiceURLBuffer, jobServiceURLBuffer, ariesServiceURLBuffer);

		baseURL = baseUrlBuffer.toString();
		reportServiceURL = reportServiceURLBuffer.toString();
		albatrossURL = albatrossURLBuffer.toString();
		candidatesURL = candidatesURLBuffer.toString();
		auditLogURL = auditLogURLBuffer.toString();
		executiveSummaryServiceURL = executiveSummaryServiceURLBuffer.toString();
		hiringPipelineServiceURL = hiringPipelineServiceURLBuffer.toString();
		jobBoardServiceURL = jobBoardServiceURLBuffer.toString();
		nymaURL = nymaURLBuffer.toString();
		nymaURLv3 = nyamURLv3Buffer.toString();
		neptuneServiceURL = neptuneServiceURLBuffer.toString();
		authServiceURL = authServiceURLBuffer.toString();
		notificationServiceURL = notificationServiceURLBuffer.toString();
		ostrichURL = ostrichURLBuffer.toString();
		baseURI = baseURIBuffer.toString();
		commURL = commURLBuffer.toString();
		invoiceServiceURL = invoiceServiceURLBuffer.toString();
		companyServiceURL = companyServiceURLBuffer.toString();
		syncFunctionURL = syncFunctionURLBuffer.toString();
		contactServiceURL = contactServiceURLBuffer.toString();
        jobServiceURL = jobServiceURLBuffer.toString();
        ariesServiceURL = ariesServiceURLBuffer.toString();
	}

	public void verifyAvailableServices(StringBuffer publicApiToken, StringBuffer albatrossToken,
			StringBuffer... servicesUrl) {
		for (int i = 0; i < servicesUrl.length; i++) {
			try {
				Response response;
				if (servicesUrl[i].toString().contains("api")) {
					response = RestClient.doGet("JSON", servicesUrl[i].toString(), "",
							publicApiToken.toString(), null, null, false);
				} else {
					response = RestClient.doGet("JSON", servicesUrl[i].toString(), "",
							albatrossToken.toString(), null, null, false);
				}
				if (response.getStatusCode() == 401) {
					int start = servicesUrl[i].indexOf(env);
					int end = start + env.length();
					servicesUrl[i] = servicesUrl[i].replace(start, end, "dev");
				}
			} catch (Exception e) {
				int start = servicesUrl[i].indexOf(env);
				int end = start + env.length();
				servicesUrl[i] = servicesUrl[i].replace(start, end, "dev");
			}
		}
	}

	static {
		// Load email addresses and passwords from CSV file
		loadEmailsFromCSV("src/main/java/io/rcrm/api/testdata/imapEmails.csv");
	}

	public static void loadEmailsFromCSV(String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 2) {
					emailAddresses.add(parts[0]);
					passwords.add(parts[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String connectToRandomEmail(Object accountid, int linkedEmailType, int isDefault, int roleId,
			String previousConnectedEmail) {
		int accountId;
		if (accountid instanceof String) {
			accountId = Integer.parseInt((String) accountid);
		} else if (accountid instanceof Integer) {
			accountId = (Integer) accountid;
		} else {
			throw new IllegalArgumentException("accountId must be either String or Integer");
		}

		int randomIndex = random.nextInt(emailAddresses.size()); // Generate random index
		String randomEmailAddress = emailAddresses.get(randomIndex);
		while (randomEmailAddress.equals(previousConnectedEmail)) {
			randomIndex = random.nextInt(emailAddresses.size());
			randomEmailAddress = emailAddresses.get(randomIndex);
		}
		String randomPassword = passwords.get(randomIndex);

		Response response = ReaperIntegration.connectEmail(accountId, randomEmailAddress, randomPassword,
				"imap", linkedEmailType, isDefault, roleId);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to connect Email");
		return randomEmailAddress;
	}

	public void connectSpecificEmail(int accountId, String email, String password, String emailType,
			int linkedEmailType, int isDefault, int roleId) {
		ReaperIntegration.connectEmail(accountId, email, password, emailType, linkedEmailType, isDefault, roleId);
	}

	public void disconnectNylasEmail(int accountId, int linkedEmailType, int notify) {
		Response response = ReaperIntegration.nylasEmailDisconnect(accountId, linkedEmailType, notify);
		Assert.assertEquals(response.getStatusCode(), 200, "Failed to disconnect email");
	}

	public void pauseEnrollment(int seqEnrollmentId) {
		ReaperIntegration.pauseEnrollment(seqEnrollmentId);
	}

	public void failScheduledEmail(int id) {
		ReaperIntegration.failScheduledEmail(id);
	}

	public static Object mergePersons(Object obj1, Object obj2) throws Exception {
		Field[] allFields = obj1.getClass().getDeclaredFields();
		for (Field field : allFields) {
			if (Modifier.isPublic(field.getModifiers()) && field.isAccessible() && field.get(obj1) == null
					&& field.get(obj2) != null) {
				field.set(obj1, field.get(obj2));
			}
		}
		return obj1;
	}

	// Convert integer to String Value
	public String integerToString(int integerValue) {
		String stringValue = String.valueOf(integerValue);
		return stringValue;
	}

	public void waitBetweenTheEveryScript(int waitTimeInMilliSeconds) {
		try {
			Thread.sleep(waitTimeInMilliSeconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static final Set<String> KNOWN_FLAGS_SET = new HashSet<>(
		java.util.Arrays.asList("contractstaffing", "automationforrevamp", "aitestparser")
	);

	private String extractFlags(String accountTypeValue) {
		if (accountTypeValue == null || accountTypeValue.isEmpty()) {
			return "";
		}

		String[] parts = accountTypeValue.split("\\|");
		StringBuilder flagName = new StringBuilder();
		for (String part : parts) {
			String trimmedPart = part.trim();
			if (trimmedPart.isEmpty()) {
				continue;
			}
			if (KNOWN_FLAGS_SET.contains(trimmedPart.toLowerCase())) {
				if (flagName.length() > 0) {
					flagName.append("|");
				}
				flagName.append(trimmedPart);
			}
		}

		return flagName.toString();
	}

	private String[] extractAccountTypeAndFlags(String accountTypeValue, String[] excludeList, 
			String[] knownFlags, String defaultAccountType, boolean useFirstPartAsAccountType) {
		String[] parts = accountTypeValue.split("\\|");
		String accountType = useFirstPartAsAccountType ? parts[0].trim() : null;
		StringBuilder flagName = new StringBuilder();
		Set<String> excludeSet = new HashSet<>();
		if (excludeList != null) {
			for (String exclude : excludeList) {
				if (exclude != null) {
					excludeSet.add(exclude.toLowerCase());
				}
			}
		}

		Set<String> knownFlagsSet = new HashSet<>();
		if (knownFlags != null) {
			for (String flag : knownFlags) {
				if (flag != null) {
					knownFlagsSet.add(flag.toLowerCase());
				}
			}
		}

		int startIndex = useFirstPartAsAccountType ? 1 : 0;
		for (int i = startIndex; i < parts.length; i++) {
			String part = parts[i].trim();
			if (part.isEmpty()) {
				continue;
			}
			String lowerPart = part.toLowerCase();
			if (excludeSet.contains(lowerPart)) {
				continue;
			}
			if (knownFlagsSet.contains(lowerPart)) {
				if (flagName.length() > 0) {
					flagName.append("|");
				}
				flagName.append(part);
				continue;
			}
			if (!useFirstPartAsAccountType && accountType == null) {
				accountType = part;
			}
		}

		return new String[] { 
			(accountType == null || accountType.isEmpty()) ? defaultAccountType : accountType,
			flagName.toString()
		};
	}

	@BeforeClass(alwaysRun = true)
	public void beforeClass() {
		AccountType accountType = getClass().getAnnotation(AccountType.class);
		if (accountType == null) {
			return;
		}

		String accountTypeValue = accountType.value();
		if (accountTypeValue == null || accountTypeValue.isEmpty()) {
			return;
		}

		String flagName = extractFlags(accountTypeValue);

		if (accountTypeValue.contains("RBAC")) {
			String[] excludeList = { "RBAC" };
			String[] knownFlags = { "automationForRevamp", "contractStaffing" };
			String[] result = extractAccountTypeAndFlags(accountTypeValue, excludeList, knownFlags, "Business", false);
			String baseAccountType = result[0];
			String finalFlagName = flagName.isEmpty() ? result[1] : flagName;

			Account account = getRbacAccounts(baseAccountType, finalFlagName, 1, true)[0];
			ThreadManager.setAccount(account);
			cacheClassAccountState(account, false);
			classesWithClassSetupRun.add(getClass());
			return;
		}

		if (accountTypeValue.contains("CrossAccount")) {
			setupCrossAccountTokens();
			classesWithClassSetupRun.add(getClass());
			return;
		}

		String lowerValue = accountTypeValue.toLowerCase();
		boolean email1 = lowerValue.contains("email1");
		boolean email2 = lowerValue.contains("email2");
		boolean hasEmail = lowerValue.contains("email") || lowerValue.contains("emailconnected");

		if (hasEmail && !email1 && !email2) {
			email1 = true;
		}

		if (lowerValue.equals("noaccount") || lowerValue.equals("notrequired") || lowerValue.equals("na")) {
			return;
		}

		boolean albatrossToken = lowerValue.contains("albatrosstkn");
		if (albatrossToken) {
			String[] excludeList = { "AlbatrossTkn" };
			String[] knownFlags = { "contractStaffing", "automationForRevamp" };
			String[] result = extractAccountTypeAndFlags(accountTypeValue, excludeList, knownFlags, "Business", true);
			accountTypeValue = result[0];
			flagName = flagName.isEmpty() ? result[1] : flagName;
		}

		Account account = getAccounts(accountTypeValue, flagName, 1)[0];
		ThreadManager.setAccount(account);
		cacheClassAccountState(account, albatrossToken);

		if (albatrossToken) {
			ThreadManager.setOwnerAlbatrossToken(account.getOwner().getToken());
			ThreadManager.setOwnerAuthCode(account.getOwner().getAuthCode());
		}

		if (email1) {
			connectToRandomEmail(account.getAccountId(), 1, 1, 4, null);
		}

		if (email2) {
			String connectedEmail1 = account.getOwner().getConnectedEmail_1();
			connectToRandomEmail(account.getAccountId(), 2,
					connectedEmail1 == null ? 1 : 0, 4, connectedEmail1);
		}
		classesWithClassSetupRun.add(getClass());
	}

	/**
	 * Ensures class-level setup runs before the first test method when tests are run by group
	 * (e.g. mvn test -DtestGroup=...), in which case TestNG may not invoke @BeforeClass.
	 * Runs TestBase.beforeClass() and any subclass method named "setUp" or "createTestData" (no-arg)
	 * at most once per test class.
	 */
	@BeforeMethod(alwaysRun = true)
	public void ensureClassSetupBeforeMethod() {
		Class<?> clazz = getClass();
		synchronized (clazz) {
			if (!classesWithClassSetupRun.contains(clazz)) {
				beforeClass();
				invokeChildClassSetupIfPresent();
				classesWithClassSetupRun.add(clazz);
			}
		}
		restoreThreadLocalAccountIfNeeded();
	}

	/**
	 * Invokes a no-arg method named "setUp" or "createTestData" on the current test instance
	 * if present (e.g. createTestData in GetCandidateWorkHistoryTest). Allows class-level
	 * test data setup to run when @BeforeClass was skipped due to group execution.
	 */
	private void invokeChildClassSetupIfPresent() {
		Class<?> clazz = getClass();
		for (String methodName : new String[] { "setUp", "createTestData" }) {
			try {
				Method m = clazz.getMethod(methodName);
				if (m.getParameterCount() == 0 && m.getDeclaringClass() != TestBase.class) {
					m.invoke(this);
					break;
				}
			} catch (NoSuchMethodException e) {
				// ignore – this class does not define setUp/createTestData
			} catch (Exception e) {
				throw new AssertionError("Failed to invoke " + methodName + " on " + clazz.getSimpleName(), e);
			}
		}
	}

	public Account[] getAccounts(String accountTypeValue, String flagName, int noOfAccounts) {
		String responseMessage = "";
		Response response;
		try {
			response = ReaperIntegration.getAccount(accountTypeValue, noOfAccounts, flagName);
			if (response.statusCode() == 200) {
				AccountList accountList = response.getBody().as(AccountList.class);
				return accountList.getAccountsList();
			} else {
				// If error message is 504, then something is wrong with the database
				responseMessage = response.getBody().prettyPrint();
				Assert.fail("Failed to get account from Reaper. Status code: " + response.statusCode()
						+ " and Response: " + (responseMessage.isEmpty() ? "No response message" : responseMessage));
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to get account from Reaper due to exception: " + e.getMessage() + "and response: "
					+ (responseMessage.isEmpty() ? "No response message" : responseMessage));
			return null;
		}
	}

	public Account[] getRbacAccounts(String accountTypeValue, String flagName, int noOfAccounts, boolean rbac) {
		String responseMessage = "";
		try {
			Response response = ReaperIntegration.getRbacAccount(accountTypeValue, flagName, noOfAccounts, rbac);
			if (response.statusCode() == 200) {
				AccountList accountList = response.getBody().as(AccountList.class);
				return accountList.getAccountsList();
			} else {
				// If error message is 504, then something is wrong with the database
				responseMessage = response.getBody().prettyPrint();
				Assert.fail("Failed to get account from Reaper. Status code: " + response.statusCode()
						+ " and Response: " + (responseMessage.isEmpty() ? "No response message" : responseMessage));
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Failed to get account from Reaper due to exception: " + e.getMessage() + "and response: "
					+ (responseMessage.isEmpty() ? "No response message" : responseMessage));
			return null;
		}
	}

	// This method can be used for specifically deactivating an account for other
	// purposes
	public void deactivateAccount(Account account) {
		if (account != null) {
			// Delete the account created for the test
			if (account.getAccountId() != 0) {
				ReaperIntegration.deactivateAccount(account.getAccountId());
			} else {
				System.out.println("Account is null. No account to deactivate.");
			}
		}
	}

	public String getAlbatrossURL() {
		return albatrossURL;
	}

	// Annotation List
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD, ElementType.TYPE })
	public @interface AccountType {
		String value() default "NA"; // Default account type is 'business'
	}

	// Common implementation for cross-account security tests
	protected String accountA_Token;
	protected String accountB_Token;
	protected String accountA_apiKey;
	protected String accountB_apiKey;
	protected Account accountA;
	protected Account accountB;

	/**
	 * Setup tokens for cross-account security tests
	 * This method should be called in @BeforeClass for cross-account tests
	 */
	protected void setupCrossAccountTokens() {
		AccountType accountType = getClass().getAnnotation(AccountType.class);
		String accountTypeValue = "Business";
		String flagName = "";
		if (accountType != null && accountType.value() != null) {
			String annotationValue = accountType.value();
			flagName = extractFlags(annotationValue);
			String[] parts = annotationValue.split("\\|");
			for (String part : parts) {
				String trimmedPart = part.trim();
				String lowerPart = trimmedPart.toLowerCase();
				if (!lowerPart.equals("crossaccount") && 
				    !lowerPart.contains("email") && 
				    !lowerPart.equals("auditlog") &&
				    !flagName.toLowerCase().contains(lowerPart)) {
					if (lowerPart.equals("business") || lowerPart.equals("free") || 
					    lowerPart.equals("enterprise") || lowerPart.equals("rbac")) {
						accountTypeValue = trimmedPart;
						break;
					}
				}
			}
		}
		accountA = getAccounts(accountTypeValue, flagName, 1)[0];
		ThreadManager.setAccount(accountA);
		accountA_Token = ThreadManager.getAlbatrossToken("Owner");
		accountA_apiKey = ThreadManager.getAccountApiKey();
		accountB = getAccounts(accountTypeValue, flagName, 1)[0];
		ThreadManager.setAccount(accountB);
		accountB_Token = ThreadManager.getAlbatrossToken("Owner");
		accountB_apiKey = ThreadManager.getAccountApiKey();
		if (accountType != null && accountType.value().contains("CrossAccount")
				&& accountType.value().contains("Email")) {
			setupEmailConnections();
		}
		if (accountType != null && accountType.value().contains("CrossAccount")
				&& accountType.value().contains("AuditLog")) {
			setupAuditLogConnections();
		}
	}

	/**
	 * Setup email connections for both accounts
	 */
	protected void setupEmailConnections() {
		// Connect email for Account A
		ThreadManager.setAccount(accountA);
		connectToRandomEmail(accountA.getAccountId(), 1, 1, 4, null);

		// Connect email for Account B
		ThreadManager.setAccount(accountB);
		connectToRandomEmail(accountB.getAccountId(), 1, 1, 4, null);
	}

	/**
	 * Get token based on account type and token type for cross-account tests
	 */
	protected String getTokenForAccount(String accountType, String tokenType) {
		if (accountType.equals("AccountA")) {
			if (tokenType.equals("valid")) {
				return accountA_Token;
			} else if (tokenType.equals("invalid")) {
				return "invalid_token_" + RandomStringUtils.randomAlphanumeric(20);
			} else if (tokenType.equals("expired")) {
				return "expired_token_" + RandomStringUtils.randomAlphanumeric(20);
			} else if (tokenType.equals("malformed")) {
				return "malformed_token_without_bearer";
			} else if (tokenType.equals("empty")) {
				return "";
			} else if (tokenType.equals("null")) {
				return null;
			}
		} else if (accountType.equals("AccountB")) {
			if (tokenType.equals("valid")) {
				return accountB_Token;
			} else if (tokenType.equals("invalid")) {
				return "invalid_token_" + RandomStringUtils.randomAlphanumeric(20);
			} else if (tokenType.equals("expired")) {
				return "expired_token_" + RandomStringUtils.randomAlphanumeric(20);
			} else if (tokenType.equals("malformed")) {
				return "malformed_token_without_bearer";
			} else if (tokenType.equals("empty")) {
				return "";
			} else if (tokenType.equals("null")) {
				return null;
			}
		} else if (accountType.equals("AccountC")) {
			return "non_existent_account_token_" + RandomStringUtils.randomAlphanumeric(20);
		}
		return ThreadManager.getOwnerAlbatrossToken();
	}

	public String[] getEmailAccountDetails() {
		String[] emailAccountDetails = new String[2];
		emailAccountDetails[0] = emailAddresses.get(4);
		emailAccountDetails[1] = passwords.get(4);
		return emailAccountDetails;
	}

	protected int getAccountId(String accountType) {
		if (accountType.equals("AccountA")) {
			ThreadManager.setAccount(accountA);
			return accountA.getAccountId();
		} else if (accountType.equals("AccountB")) {
			ThreadManager.setAccount(accountB);
			return accountB.getAccountId();
		} else {
			return 0;
		}
	}

	public String getAccountApiKey(String accountType) {
		if (accountType.equals("AccountA")) {
			return accountA_apiKey;
		} else if (accountType.equals("AccountB")) {
			return accountB_apiKey;
		} else {
			return null;
		}
	}

	protected void setupAuditLogConnections() {
		// Enable audit log for Account A
		ThreadManager.setAccount(accountA);
		ReaperIntegration.enableAuditLog(accountA.getAccountId());
		// Enable audit log for Account B
		ThreadManager.setAccount(accountB);
		ReaperIntegration.enableAuditLog(accountB.getAccountId());
	}

	public String getRoleBasedToken(String accountType, String role) {
		String roleBasedToken = "";
		if (accountType.equals("AccountA")) {
			ThreadManager.setAccount(accountA);
		} else if (accountType.equals("AccountB")) {
			ThreadManager.setAccount(accountB);
		}
		switch (role) {
			case "Owner":
				roleBasedToken = ThreadManager.getAlbatrossToken("Owner");
				break;
			case "Admin":
				roleBasedToken = ThreadManager.getAlbatrossToken("Admin");
				break;
			case "Team Member":
				roleBasedToken = ThreadManager.getAlbatrossToken("TeamMember");
				break;
			case "Restricted":
				roleBasedToken = ThreadManager.getAlbatrossToken("RestrictedTeamMember");
				break;
		}
		return roleBasedToken;
	}

	protected void setupRbacTokensAndUserIds(Map<String, String> tokenMap, Map<String, Integer> userIdMap) {
		if (ThreadManager.getOwner() != null) {
			tokenMap.put("AccountOwner", ThreadManager.getOwner().getToken());
			userIdMap.put("AccountOwner", ThreadManager.getOwner().getUserId());
		}
		if (ThreadManager.getAdmin() != null) {
			tokenMap.put("Admin", ThreadManager.getAdmin().getToken());
			userIdMap.put("Admin", ThreadManager.getAdmin().getUserId());
		}
		if (ThreadManager.getTeamMember() != null) {
			tokenMap.put("TeamMember", ThreadManager.getTeamMember().getToken());
			userIdMap.put("TeamMember", ThreadManager.getTeamMember().getUserId());
		}
		if (ThreadManager.getRestrictedTeamMember() != null) {
			tokenMap.put("RestrictedTeamMember", ThreadManager.getRestrictedTeamMember().getToken());
			userIdMap.put("RestrictedTeamMember", ThreadManager.getRestrictedTeamMember().getUserId());
		}
		if (ThreadManager.getCustomRoleTeamOnly() != null) {
			tokenMap.put("CustomRoleTeamOnly", ThreadManager.getCustomRoleTeamOnly().getToken());
			userIdMap.put("CustomRoleTeamOnly", ThreadManager.getCustomRoleTeamOnly().getUserId());
		}
		if (ThreadManager.getCustomRoleNothing() != null) {
			tokenMap.put("CustomRoleNothing", ThreadManager.getCustomRoleNothing().getToken());
			userIdMap.put("CustomRoleNothing", ThreadManager.getCustomRoleNothing().getUserId());
		}
	}

	public JSONObject readJsonFileFromPath(String filePath) {
		try {
			String content = new String(Files.readAllBytes(Paths.get(filePath)));
			return new JSONObject(content);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Failed to read JSON file from path: " + filePath + ". Error: " + e.getMessage());
			return null;
		}
	}

	public int getRoleBasedId(String accountType, String role) {
		int roleBasedId = 0;
		if (accountType.equals("AccountA")) {
			ThreadManager.setAccount(accountA);
		} else if (accountType.equals("AccountB")) {
			ThreadManager.setAccount(accountB);
		}
		String roleKey;
		switch (role) {
			case "Owner":
				roleKey = "Owner";
				break;
			case "Admin":
				roleKey = "Admin";
				break;
			case "Team Member":
				roleKey = "TeamMember";
				break;
			case "Restricted":
				roleKey = "RestrictedTeamMember";
				break;
			default:
				return roleBasedId;
		}
		String[] tokenAndUserId = ThreadManager.getAlbatrossTokenAndUserId(roleKey);
		if (tokenAndUserId != null && tokenAndUserId.length >= 2) {
			roleBasedId = Integer.parseInt(tokenAndUserId[1]);
		}
		return roleBasedId;
	}
}