package io.rcrm.api.pojo;

public class Hotlist {

	private String name;
	private String related_to_type;
	private int shared;

	public Hotlist() {
	}

	// getter and setter methods:

	public String getName() {
		return name;
	}

	public void setFirst_name(String name) {
		this.name = name;
	}

	public String getRelated_to_type() {
		return related_to_type;
	}

	public void setRelated_to_type(String related_to_type) {
		this.related_to_type = related_to_type;
	}

	public int getShared() {
		return shared;
	}

	public void setShared(int shared) {
		this.shared = shared;
	}

}
