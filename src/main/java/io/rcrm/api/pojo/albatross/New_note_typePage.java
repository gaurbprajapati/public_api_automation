package io.rcrm.api.pojo.albatross;

public class New_note_typePage {
	
	private int id;
	private String label;
	private int defaultvalue;
	private int is_custom;
	private boolean deleted;
	
	
	public New_note_typePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public New_note_typePage(int id, String label, int defaultvalue, int is_custom, boolean deleted) {
		super();
		this.id = id;
		this.label = label;
		this.defaultvalue = defaultvalue;
		this.is_custom = is_custom;
		this.deleted = deleted;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getDefault() {
		return defaultvalue;
	}
	public void setDefault(int defaultvalue) {
		this.defaultvalue = defaultvalue;
	}
	public int getIs_custom() {
		return is_custom;
	}
	public void setIs_custom(int is_custom) {
		this.is_custom = is_custom;
	}
	public boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
