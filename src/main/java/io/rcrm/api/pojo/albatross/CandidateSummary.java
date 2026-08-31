package io.rcrm.api.pojo.albatross;

public class CandidateSummary {

	private String summary_content_html;
	private String action;
	private String file_title;

	public CandidateSummary() {
		super();
	}

	public String getSummary_content_html() {
		return summary_content_html;
	}

	public void setSummary_content_html(String summary_content_html) {
		this.summary_content_html = summary_content_html;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getFile_title() {
		return file_title;
	}

	public void setFile_title(String file_title) {
		this.file_title = file_title;
	}

}
