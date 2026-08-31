package io.rcrm.api.pojo.albatross;

import java.util.List;

public class UpdateFields {

	private String key;
	private String value;
	private String tableFlag;
	private List<Integer> id;
	private boolean addInValues;

	public UpdateFields() {
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTableFlag() {
		return tableFlag;
	}

	public void setTableFlag(String tableFlag) {
		this.tableFlag = tableFlag;
	}

	public List<Integer> getId() {
		return id;
	}

	public void setId(List<Integer> id) {
		this.id = id;
	}

	public boolean isAddInValues() {
		return addInValues;
	}

	public void setAddInValues(boolean addInValues) {
		this.addInValues = addInValues;
	}
}