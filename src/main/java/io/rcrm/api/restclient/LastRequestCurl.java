package io.rcrm.api.restclient;

/**
 * Holds last HTTP request as curl for current test thread (set by {@link CurlCaptureFilter}).
 */
public final class LastRequestCurl {

	/** TestNG stores the last curl under this key on failed tests for the Extent HTML report. */
	public static final String ITEST_ATTR_LAST_CURL = "io.rcrm.api.restclient.lastCurl";

	private static final ThreadLocal<String> LAST = new ThreadLocal<String>();

	private LastRequestCurl() {
	}

	public static void set(String curl) {
		LAST.set(curl);
	}

	public static String get() {
		return LAST.get();
	}

	public static void clear() {
		LAST.remove();
	}
}
