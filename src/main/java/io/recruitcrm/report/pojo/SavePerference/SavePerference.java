package io.recruitcrm.report.pojo.SavePerference;

public class SavePerference {

	private String name;
	private int report_type;
	private Settings settings;
	
	public SavePerference() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SavePerference(String name, int report_type, Settings settings) {
		super();
		this.name = name;
		this.report_type = report_type;
		this.settings = settings;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReport_type() {
		return report_type;
	}

	public void setReport_type(int report_type) {
		this.report_type = report_type;
	}
	
	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

}
