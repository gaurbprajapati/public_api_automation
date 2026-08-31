package io.recruitcrm.albatross.company;

import io.rcrm.api.pojo.Contact;

public class Company {

	private String companyname;
	private String aboutcompany;
	private String city;
	private int industryid;
	private String website;
	private String address;
	private String profilelinkedin;
	private Contact contact;
	private int owner_id;
	private int created_by;


	public Company() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Company(String companyname, String aboutcompany, String city, int industryid, String website, String address) {
		super();
		this.companyname = companyname;
		this.aboutcompany = aboutcompany;
		this.city = city;
		this.industryid = industryid;
		this.website = website;
		this.address = address;
	}
	public String getCompanyname() {
		return companyname;
	}

	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}
	
	public String getAboutcompany() {
		return aboutcompany;
	}

	public void setAboutcompany(String aboutcompany) {
		this.aboutcompany = aboutcompany;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getIndustryid() {
		return industryid;
	}

	public void setIndustryid(int industryid) {
		this.industryid = industryid;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProfilelinkedin() {
		return profilelinkedin;
	}
	
	public void setProfilelinkedin(String profilelinkedin) {
		this.profilelinkedin = profilelinkedin;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public int getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(int owner_id) {
		this.owner_id = owner_id;
	}

	public int getCreated_by() {
		return created_by;
	}

	public void setCreated_by(int created_by) {
		this.created_by = created_by;
	}

}
