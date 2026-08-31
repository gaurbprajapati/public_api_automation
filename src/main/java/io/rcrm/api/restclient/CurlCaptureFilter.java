package io.rcrm.api.restclient;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Records each outgoing request as curl on the current thread (last write wins).
 * Reaper test-tracking calls ({@link com.qa.api.util.reaper.TestTrackingUtil}) are excluded so they
 * do not replace the failing API request after {@code onTestFailure} listeners run.
 */
public final class CurlCaptureFilter implements Filter {

	/** Reaper paths used for CI test status; not part of the API under test. */
	static final String REAPER_TEST_TRACKING_PATH = "/api/test-updates/";

	private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

	public static void register() {
		if (REGISTERED.compareAndSet(false, true)) {
			RestAssured.filters(new CurlCaptureFilter());
		}
	}

	/** Max characters of response body included in report sample (rest is truncated). */
	static final int MAX_RESPONSE_BODY_CHARS = 32_768;

	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		if (isExcludedFromFailureCapture(requestSpec)) {
			return ctx.next(requestSpec, responseSpec);
		}
		LastRequestResponse.clear();
		try {
			LastRequestCurl.set(CurlBuilder.toCurl(requestSpec));
		} catch (RuntimeException ignored) {
			LastRequestCurl.set("# curl capture failed for this request");
		}
		Response response = ctx.next(requestSpec, responseSpec);
		try {
			LastRequestResponse.set(buildResponseSample(response));
		} catch (RuntimeException ex) {
			LastRequestResponse.set("# response capture failed: " + ex.getMessage());
		}
		return response;
	}

	static boolean isExcludedFromFailureCapture(FilterableRequestSpecification requestSpec) {
		if (requestSpec == null) {
			return false;
		}
		String uri = requestSpec.getURI();
		if (uri == null || uri.isEmpty()) {
			return false;
		}
		return uri.toLowerCase(Locale.ROOT).contains(REAPER_TEST_TRACKING_PATH);
	}

	static String buildResponseSample(Response response) {
		if (response == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		String statusLine = response.getStatusLine();
		if (statusLine != null && !statusLine.trim().isEmpty()) {
			sb.append(statusLine.trim());
		} else {
			sb.append("HTTP ").append(response.getStatusCode());
		}
		sb.append('\n');
		String contentType = response.getContentType();
		if (contentType != null && !contentType.isEmpty()) {
			sb.append("Content-Type: ").append(contentType).append("\n\n");
		} else {
			sb.append('\n');
		}
		String body = response.getBody() != null ? response.getBody().asString() : "";
		if (body.length() > MAX_RESPONSE_BODY_CHARS) {
			sb.append(body, 0, MAX_RESPONSE_BODY_CHARS);
			sb.append("\n\n... [truncated: ").append(body.length()).append(" characters total]");
		} else {
			sb.append(body);
		}
		return sb.toString();
	}
}
