package io.recruitcrm.report.pojo;

public class KpiLists {

	public KpiLists() {
		// TODO Auto-generated constructor stub
		super();
	}

	private String value;
	private String label;
	private boolean checked;

	public KpiLists(String value, String label, boolean checked) {
		super();
		this.value = value;
		this.label = label;
		this.checked = checked;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	

}
