package io.rcrm.api.pojo.albatross.Contact;

public class EntityViewLockSettings {
	private String key;
	private String value = "{\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":1,\"7\":0,\"8\":0,\"9\":0,\"10\":0,\"15\":0,\"16\":0,\"17\":0,\"18\":0,\"19\":0,\"20\":0,\"21\":0,\"22\":0,\"25\":0,\"26\":0,\"27\":0}";
	private String tableFlag = "accountsettings";
	private int id;
	private boolean isSilentProcess = true;

	// Getters and Setters
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isSilentProcess() {
		return isSilentProcess;
	}

	public void setSilentProcess(boolean silentProcess) {
		isSilentProcess = silentProcess;
	}

}
