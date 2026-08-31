package io.rcrm.api.pojo.nyma;

import java.util.ArrayList;

public class EmailSignature {
	private String key;
	private String value;
	private String tableFlag;
	private int id;

	public EmailSignature() {

	}
	public EmailSignature(String key, String value, String tableFlag, int id) {
		super();
		this.id = id;
		this.key = key;
		this.value = value;
		this.tableFlag = tableFlag;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
}

