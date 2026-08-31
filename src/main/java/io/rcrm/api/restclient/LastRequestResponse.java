package io.rcrm.api.restclient;

/**
 * Holds last HTTP response text sample for current test thread (set by {@link CurlCaptureFilter}).
 */
public final class LastRequestResponse {

	/** TestNG stores the last response sample under this key on failed tests for reports. */
	public static final String ITEST_ATTR_LAST_RESPONSE = "io.rcrm.api.restclient.lastResponse";

	private static final ThreadLocal<String> LAST = new ThreadLocal<String>();

	private LastRequestResponse() {
	}

	public static void set(String sample) {
		LAST.set(sample);
	}

	public static String get() {
		return LAST.get();
	}

	public static void clear() {
		LAST.remove();
	}
}
