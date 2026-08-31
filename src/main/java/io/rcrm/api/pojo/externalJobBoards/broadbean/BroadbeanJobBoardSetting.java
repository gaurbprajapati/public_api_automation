package io.rcrm.api.pojo.externalJobBoards.broadbean;

public class BroadbeanJobBoardSetting {
	private String accountalias;
	private String clientid;
	private String secret;
	private String adc_username;
	private String accountType;
	private String retrievalType;
	private String selectedGreenOption;
	private String selectedYellowOption;
	private String selectedRedOption;

	public BroadbeanJobBoardSetting() {
		super();
	}

	public BroadbeanJobBoardSetting(String accountalias, String clientid, String secret, String adc_username, String accountType, String retrievalType, String selectedGreenOption, String selectedYellowOption, String selectedRedOption) {
		this.accountalias = accountalias;
		this.clientid = clientid;
		this.secret = secret;
		this.adc_username = adc_username;
		this.accountType = accountType;
		this.retrievalType = retrievalType;
		this.selectedGreenOption = selectedGreenOption;
		this.selectedYellowOption = selectedYellowOption;
		this.selectedRedOption = selectedRedOption;
	}

	public BroadbeanJobBoardSetting(String accountalias, String clientid, String secret, String adc_username) {
		this.accountalias = accountalias;
		this.clientid = clientid;
		this.secret = secret;
		this.adc_username = adc_username;
	}

	public String getAccountalias() {
		return accountalias;
	}

	public void setAccountalias(String accountalias) {
		this.accountalias = accountalias;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getAdc_username() {
		return adc_username;
	}

	public void setAdc_username(String adc_username) {
		this.adc_username = adc_username;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getRetrievalType() {
		return retrievalType;
	}

	public void setRetrievalType(String retrievalType) {
		this.retrievalType = retrievalType;
	}

	public String getSelectedGreenOption() {
		return selectedGreenOption;
	}

	public void setSelectedGreenOption(String selectedGreenOption) {
		this.selectedGreenOption = selectedGreenOption;
	}

	public String getSelectedYellowOption() {
		return selectedYellowOption;
	}

	public void setSelectedYellowOption(String selectedYellowOption) {
		this.selectedYellowOption = selectedYellowOption;
	}

	public String getSelectedRedOption() {
		return selectedRedOption;
	}

	public void setSelectedRedOption(String selectedRedOption) {
		this.selectedRedOption = selectedRedOption;
	}
}
