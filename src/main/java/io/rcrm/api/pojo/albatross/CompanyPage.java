package io.rcrm.api.pojo.albatross;

public class CompanyPage {
	
	private String slug;
    private String companyname;
    private String aboutcompany;
    private int industryid;
    private String website;
    private String city;
    private String address;
    private String profilefacebook;
    private String profiletwitter;
    private String profilelinkedin;
    
    
	public CompanyPage(String slug, String companyname, String aboutcompany, int industryid, String website,
			String city, String address) {
		super();
		this.slug = slug;
		this.companyname = companyname;
		this.aboutcompany = aboutcompany;
		this.industryid = industryid;
		this.website = website;
		this.city = city;
		this.address = address;
	}


	public String getSlug() {
		return slug;
	}


	public void setSlug(String slug) {
		this.slug = slug;
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
