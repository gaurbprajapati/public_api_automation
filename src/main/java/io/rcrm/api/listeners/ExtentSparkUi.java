package io.rcrm.api.listeners;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;

/**
 * Shared Spark reporter tuning (dashboard, timeline) and HTML helpers for log lines.
 */
public final class ExtentSparkUi {

	/**
	 * Injected with dashboard JS (or alone when no tests). Delegated click on {@code .extent-copy-block}
	 * copies sibling {@code pre} inside same {@code details}; capture phase avoids toggling {@code details}.
	 */
	private static final String SPARK_COPY_PRE_BUTTON_JS = "(function(){document.addEventListener(\"click\",function(e){"
			+ "var t=e.target;if(!t||!t.classList||!t.classList.contains(\"extent-copy-block\"))return;"
			+ "e.preventDefault();e.stopPropagation();var d=t.closest(\"details\");var p=d&&d.querySelector(\"pre\");"
			+ "if(!p)return;var txt=p.textContent||\"\";function ok(){t.textContent=\"Copied\";"
			+ "setTimeout(function(){t.textContent=\"Copy\";},1600);}function fb(){var ta=document.createElement(\"textarea\");"
			+ "ta.value=txt;ta.setAttribute(\"readonly\",\"\");ta.style.position=\"fixed\";ta.style.left=\"-9999px\";"
			+ "document.body.appendChild(ta);ta.select();try{document.execCommand(\"copy\");}catch(x){}"
			+ "document.body.removeChild(ta);ok();}"
			+ "if(navigator.clipboard&&navigator.clipboard.writeText){navigator.clipboard.writeText(txt).then(ok).catch(fb);}"
			+ "else{fb();}},true);})();";

	private ExtentSparkUi() {
	}

	/**
	 * {@code target/surefire-reports} under the Maven module root. Uses {@code basedir} when set (Surefire fork),
	 * otherwise {@code user.dir}, so reports do not follow a wrong working directory (for example {@code build/}).
	 */
	public static Path surefireReportDir() {
		String base = System.getProperty("basedir");
		if (base == null || base.trim().isEmpty()) {
			base = System.getProperty("user.dir", ".");
		}
		return Paths.get(base, "target", "surefire-reports").normalize();
	}

	/**
	 * Prepends Selenium-style metrics (pass %, execution time, total scripts) into the parent chart card footer,
	 * same layout as {@code card-footer} metrics in the UI automation project.
	 */
	public static void applyDashboardMetricsFooter(ExtentSparkReporter spark, long passed, long failed, long skipped,
			long durationMillis) {
		long total = passed + failed + skipped;
		if (total <= 0) {
			spark.config().setJs(SPARK_COPY_PRE_BUTTON_JS);
			return;
		}
		double passPct = 100.0 * passed / total;
		String durationFormatted = formatDurationHms(durationMillis < 0 ? 0L : durationMillis);
		String innerHtml = buildDashboardMetricsInnerHtml(passPct, total, durationFormatted);
		String escapedInner = escapeJsDoubleQuoted(innerHtml);
		String dashJs = "(function(){function run(){var c=document.getElementById('parent-analysis');if(!c)return;"
				+ "var card=c.closest('.card');if(!card)return;"
				+ "var footer=card.querySelector('.card-footer');if(!footer||footer.querySelector('.extent-metrics-banner'))return;"
				+ "var wrap=document.createElement('div');wrap.className='extent-metrics-banner';"
				+ "wrap.style.marginBottom='10px';wrap.innerHTML=\"" + escapedInner + "\";"
				+ "footer.insertBefore(wrap,footer.firstChild);}"
				+ "if(document.readyState==='loading'){document.addEventListener('DOMContentLoaded',run);}else{run();}"
				+ "})();";
		spark.config().setJs(SPARK_COPY_PRE_BUTTON_JS + dashJs);
	}

	private static String buildDashboardMetricsInnerHtml(double passPct, long totalScripts, String durationFormatted) {
		return "<div style=\"margin-bottom: 10px;\">"
				+ "<div style=\"display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 14px;\">"
				+ "<span>Pass Percentage:</span>"
				+ "<span style=\"font-weight: bold; color: #007bff;\">"
				+ String.format(Locale.US, "%.2f%%", passPct)
				+ "</span></div>"
				+ "<div style=\"display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 14px;\">"
				+ "<span>Execution Time:</span>"
				+ "<span style=\"font-weight: bold; color: #007bff;\">"
				+ escapeHtml(durationFormatted)
				+ "</span></div>"
				+ "<div style=\"display: flex; justify-content: space-between; margin-bottom: 5px; font-size: 14px;\">"
				+ "<span>Total Scripts:</span>"
				+ "<span style=\"font-weight: bold; color: #007bff;\">"
				+ totalScripts
				+ "</span></div>"
				+ "</div>";
	}

	/** Wall duration as {@code 0h 52m 57s 139ms} (matches UI automation report). */
	public static String formatDurationHms(long millis) {
		long ms = millis < 0 ? 0L : millis;
		long hours = TimeUnit.MILLISECONDS.toHours(ms);
		ms -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
		ms -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
		ms -= TimeUnit.SECONDS.toMillis(seconds);
		return String.format(Locale.US, "%dh %dm %ds %dms", hours, minutes, seconds, ms);
	}

	private static String escapeJsDoubleQuoted(String s) {
		StringBuilder b = new StringBuilder(s.length() + 8);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '\\':
					b.append("\\\\");
					break;
				case '"':
					b.append("\\\"");
					break;
				case '\n':
					b.append("\\n");
					break;
				case '\r':
					b.append("\\r");
					break;
				default:
					b.append(c);
			}
		}
		return b.toString();
	}

	/**
	 * Timeline + default view order so dashboard (pie, pass %) shows first; charts use test status counts.
	 */
	public static void configureSpark(ExtentSparkReporter spark) {
		spark.config().setTheme(Theme.STANDARD);
		spark.config().setTimelineEnabled(true);
		spark.viewConfigurer()
				.viewOrder()
				.as(Arrays.asList(
						ViewName.DASHBOARD,
						ViewName.CATEGORY,
						ViewName.TEST,
						ViewName.EXCEPTION,
						ViewName.LOG))
				.apply();
	}

	/**
	 * Collapsible block (native {@code details}) with escaped body for safe HTML logs. No copy control.
	 *
	 * @return HTML string, or null if {@code rawText} is null/empty
	 */
	public static String collapsiblePre(String summary, String rawText) {
		return collapsiblePre(summary, rawText, false);
	}

	/**
	 * Same as {@link #collapsiblePre(String, String)}; when {@code copyButton} is true, adds a Copy control
	 * (works with {@link #applyDashboardMetricsFooter} / report {@code setJs}).
	 */
	public static String collapsiblePre(String summary, String rawText, boolean copyButton) {
		if (rawText == null || rawText.isEmpty()) {
			return null;
		}
		String copyRow = copyButton
				? "<div style=\"text-align:right;margin:4px 0 2px;\">"
						+ "<button type=\"button\" class=\"extent-copy-block\" style=\"font-size:12px;padding:4px 12px;"
						+ "cursor:pointer;border:1px solid #c9d1d9;border-radius:6px;background:#f6f8fa;\">Copy</button>"
						+ "</div>"
				: "";
		return "<details class=\"extent-http-capture\" style=\"margin:8px 0;border:1px solid #e1e4e8;border-radius:6px;padding:2px 8px;\">"
				+ "<summary style=\"cursor:pointer;font-weight:600;outline:none;\">"
				+ escapeHtml(summary)
				+ "</summary>"
				+ copyRow
				+ "<pre style=\"margin:8px 0 4px;max-height:400px;overflow:auto;font-size:12px;line-height:1.45;"
				+ "white-space:pre-wrap;word-break:break-word;background:#f6f8fa;padding:10px;border-radius:4px;\">"
				+ escapeHtml(rawText)
				+ "</pre></details>";
	}

	private static String escapeHtml(String s) {
		if (s == null || s.isEmpty()) {
			return "";
		}
		StringBuilder out = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '&':
					out.append("&amp;");
					break;
				case '<':
					out.append("&lt;");
					break;
				case '>':
					out.append("&gt;");
					break;
				case '"':
					out.append("&quot;");
					break;
				default:
					out.append(c);
			}
		}
		return out.toString();
	}
}
