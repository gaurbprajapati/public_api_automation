package io.rcrm.api.restclient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.MultiPartSpecification;

/**
 * Builds a bash-style curl from Rest-Assured's merged request spec.
 */
public final class CurlBuilder {

	private CurlBuilder() {
	}

	public static String toCurl(FilterableRequestSpecification spec) {
		StringBuilder sb = new StringBuilder("curl");
		String method = spec.getMethod();
		sb.append(" -X ").append(method == null ? "GET" : method);
		sb.append(" ").append(shellQuote(spec.getURI()));

		for (Header h : spec.getHeaders().asList()) {
			sb.append(" -H ").append(shellQuote(h.getName() + ": " + h.getValue()));
		}
		for (Cookie c : spec.getCookies().asList()) {
			sb.append(" -H ").append(shellQuote("Cookie: " + c.getName() + "=" + c.getValue()));
		}

		List<MultiPartSpecification> parts = spec.getMultiPartParams();
		if (parts != null && !parts.isEmpty()) {
			sb.append(" # multipart: ").append(parts.size())
					.append(" part(s); use request log for full details");
			return sb.toString();
		}

		Map<String, String> form = spec.getFormParams();
		if (form != null && !form.isEmpty()) {
			for (Map.Entry<String, String> e : form.entrySet()) {
				sb.append(" --data-urlencode ").append(shellQuote(e.getKey() + "=" + e.getValue()));
			}
			return sb.toString();
		}

		Object body = spec.getBody();
		if (body != null) {
			String bodyStr = bodyToString(body);
			if (bodyStr != null && !bodyStr.isEmpty()) {
				sb.append(" -d ").append(shellQuote(bodyStr));
			}
		}
		return sb.toString();
	}

	private static String bodyToString(Object body) {
		if (body instanceof byte[]) {
			return new String((byte[]) body, StandardCharsets.UTF_8);
		}
		if (body instanceof String) {
			return (String) body;
		}
		return String.valueOf(body);
	}

	/** Bash single-quoted string. */
	static String shellQuote(String s) {
		if (s == null) {
			return "''";
		}
		return "'" + s.replace("'", "'\\''") + "'";
	}
}
