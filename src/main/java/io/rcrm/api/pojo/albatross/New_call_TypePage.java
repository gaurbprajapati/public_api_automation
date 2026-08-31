package io.rcrm.api.pojo.albatross;

public class New_call_TypePage {
	
	private int defaultvalue;
	private int id;
	private String label;
    private boolean deleted;
	
	public New_call_TypePage() {
		super();
		// TODO Auto-generated constructor stub
	}

	

	public New_call_TypePage(int defaultvalue, int id, String label, boolean deleted) {
		super();
		this.defaultvalue = defaultvalue;
		this.id = id;
		this.label = label;
		this.deleted = deleted;
	}



	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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

	
	public void setDefaultvalue(int defaultvalue) {
		this.defaultvalue = defaultvalue;
	}
	

}
