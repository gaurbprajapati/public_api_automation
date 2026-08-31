package io.rcrm.api.pojo.executiveSummary;

public class GenerateReport {

	private String report_content_html="<html><body><h1>Hello World</h1></body></html>";
	private String action;

	public GenerateReport() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GenerateReport(String report_content_html, String action) {
		super();
		this.report_content_html = report_content_html;
		this.action = action;
	}

	public String getReport_content_html() {
		return report_content_html;
	}

	public void setReport_content_html(String report_content_html) {
		this.report_content_html = report_content_html;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
