package io.recruitcrm.albatross.contact;

public class Contact {

	private String stageid;
	private String contactnumber;
	private String firstname;
	private String lastname;
	private String email;

	public Contact(String firstname, String lastname, String email, String city, String contactnumber, String stageid) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.city = city;
		this.contactnumber = contactnumber;
		this.stageid = stageid;
	}

	public String getStageid() {
		return stageid;
	}

	public void setStageid(String stageid) {
		this.stageid = stageid;
	}

	public String getContactnumber() {
		return contactnumber;
	}

	public void setContactnumber(String contactnumber) {
		this.contactnumber = contactnumber;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	private String city;

	public Contact() {
		super();
	}

}
