package io.rcrm.api.pojo.chromeExtension;

public class Companies {

	private Company company;
	private Contact contact;
	private boolean extension_request = true;
	private boolean overrideData;
	private Object fileTypeCustomFieldsForCompany;
	private Object fileTypeCustomFieldsForCompanyContact;

	public static class Company {
		private String profilelinkedin;
		private String companyname;
		private String website;
		private String aboutcompany;
		private String fulladdress;
		private String logo;

		public Company() {

		}

		public Company(String companyname, String website, String aboutcompany, String fulladdress) {
			super();
			this.companyname = companyname;
			this.website = website;
			this.aboutcompany = aboutcompany;
			this.fulladdress = fulladdress;
		}

		// Getters and Setters
		public String getProfilelinkedin() {
			return profilelinkedin;
		}

		public void setProfilelinkedin(String profilelinkedin) {
			this.profilelinkedin = profilelinkedin;
		}

		public String getCompanyname() {
			return companyname;
		}

		public void setCompanyname(String companyname) {
			this.companyname = companyname;
		}

		public String getWebsite() {
			return website;
		}

		public void setWebsite(String website) {
			this.website = website;
		}

		public String getAboutcompany() {
			return aboutcompany;
		}

		public void setAboutcompany(String aboutcompany) {
			this.aboutcompany = aboutcompany;
		}

		public String getFulladdress() {
			return fulladdress;
		}

		public void setFulladdress(String fulladdress) {
			this.fulladdress = fulladdress;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}
	}

	public static class Contact {
		private String profilelinkedin;
		private String profiletwitter;
		private String firstname;
		private String lastname;
		private String designation;
		private String email;
		private String contactnumber;
		private String locality;

		public Contact() {

		}

		public Contact(String firstname, String lastname, String designation, String email, String contactnumber) {
			super();
			this.firstname = firstname;
			this.lastname = lastname;
			this.designation = designation;
			this.email = email;
			this.contactnumber = contactnumber;
		}

		// Getters and Setters
		public String getProfilelinkedin() {
			return profilelinkedin;
		}

		public void setProfilelinkedin(String profilelinkedin) {
			this.profilelinkedin = profilelinkedin;
		}

		public String getProfiletwitter() {
			return profiletwitter;
		}

		public void setProfiletwitter(String profiletwitter) {
			this.profiletwitter = profiletwitter;
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

		public String getLocality() {
			return locality;
		}

		public void setLocality(String locality) {
			this.locality = locality;
		}
	}

	// Getters and Setters
	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public boolean isExtension_request() {
		return extension_request;
	}

	public void setExtension_request(boolean extension_request) {
		this.extension_request = extension_request;
	}

	public boolean isOverrideData() {
		return overrideData;
	}

	public void setOverrideData(boolean overrideData) {
		this.overrideData = overrideData;
	}

	public Object getFileTypeCustomFieldsForCompany() {
		return fileTypeCustomFieldsForCompany;
	}

	public void setFileTypeCustomFieldsForCompany(Object fileTypeCustomFieldsForCompany) {
		this.fileTypeCustomFieldsForCompany = fileTypeCustomFieldsForCompany;
	}

	public Object getFileTypeCustomFieldsForCompanyContact() {
		return fileTypeCustomFieldsForCompanyContact;
	}

	public void setFileTypeCustomFieldsForCompanyContact(Object fileTypeCustomFieldsForCompanyContact) {
		this.fileTypeCustomFieldsForCompanyContact = fileTypeCustomFieldsForCompanyContact;
	}

}