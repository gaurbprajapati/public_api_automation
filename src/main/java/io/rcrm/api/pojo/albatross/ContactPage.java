package io.rcrm.api.pojo.albatross;

public class ContactPage {
	
	private String id;
    private String slug;
    private String firstname;
    private String lastname;
    private String designation;
    private String email;
    private String contactnumber;
    private String city;
    private String address;
    private String profilefacebook;
    private String profiletwitter;
    private String profilelinkedin;
    
	public ContactPage(String id, String slug, String firstname, String lastname, String designation, String email,
			String contactnumber, String city, String address, String profilefacebook, String profiletwitter,
			String profilelinkedin) {
		super();
		this.id = id;
		this.slug = slug;
		this.firstname = firstname;
		this.lastname = lastname;
		this.designation = designation;
		this.email = email;
		this.contactnumber = contactnumber;
		this.city = city;
		this.address = address;
		this.profilefacebook = profilefacebook;
		this.profiletwitter = profiletwitter;
		this.profilelinkedin = profilelinkedin;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
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

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactnumber() {
		return contactnumber;
	}

	public void setContactnumber(String contactnumber) {
		this.contactnumber = contactnumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProfilefacebook() {
		return profilefacebook;
	}

	public void setProfilefacebook(String profilefacebook) {
		this.profilefacebook = profilefacebook;
	}

	public String getProfiletwitter() {
		return profiletwitter;
	}

	public void setProfiletwitter(String profiletwitter) {
		this.profiletwitter = profiletwitter;
	}

	public String getProfilelinkedin() {
		return profilelinkedin;
	}

	public void setProfilelinkedin(String profilelinkedin) {
		this.profilelinkedin = profilelinkedin;
	}  

}
