package io.rcrm.api.pojo.albatross.emailtrigger;

public class Emailtriggersetting {

	private int id;
	private String name;
	private int trigger;
	private int value;
	private int emailsettinguser;

	public Emailtriggersetting(int id, String name, int trigger, int value, int emailsettinguser) {
		this.emailsettinguser = emailsettinguser;
		this.value = value;
		this.trigger = trigger;
		this.name = name;
		this.id = id;
	}

	public Emailtriggersetting(String name, int trigger, int value, int emailsettinguser) {
		this.emailsettinguser = emailsettinguser;
		this.value = value;
		this.trigger = trigger;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setName(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTrigger() {
		return trigger;
	}

	public void setTrigger(int trigger) {
		this.trigger = trigger;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getEmailsettinguser() {
		return emailsettinguser;
	}

	public void setEmailsettinguser(int emailsettinguser) {
		this.emailsettinguser = emailsettinguser;
	}

}
