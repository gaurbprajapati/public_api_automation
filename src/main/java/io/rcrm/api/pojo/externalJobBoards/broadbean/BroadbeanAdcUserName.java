package io.rcrm.api.pojo.externalJobBoards.broadbean;

public class BroadbeanAdcUserName {
	private int connection_id;
	private String adc_username;

	public BroadbeanAdcUserName() {
		super();
	}

	public BroadbeanAdcUserName(int connection_id, String adc_username) {
		this.connection_id = connection_id;
		this.adc_username = adc_username;
	}

	public int getConnection_id() {
		return connection_id;
	}

	public void setConnection_id(int connection_id) {
		this.connection_id = connection_id;
	}

	public String getAdc_username() {
		return adc_username;
	}

	public void setAdc_username(String adc_username) {
		this.adc_username = adc_username;
	}
}
