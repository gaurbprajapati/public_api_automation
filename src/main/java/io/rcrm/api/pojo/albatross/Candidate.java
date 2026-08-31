package io.rcrm.api.pojo.albatross;

public class Candidate {
	
	private boolean id;
    private String slug;
    private String firstname;
    private String lastname;
    private String emailid;
    private int genderid;
    private String contactnumber;
    private String address;
    private String city;
    private String summary;
    private String locality;
    private int qualificationid;
    private String specialization;
    private int workexpyr;
    private int workexpmonth;
    private int relevantexperience;
    private String position;
    private String availablefrom;
    private int currentsalary;
    private String lastorganisation;
    private String skill;
    private int willingtorelocate;
    private int salaryexpectation;
    private int salarytype;
    private String currentstatus;
    private int noticeperiod;
    private int currencyid;
    private String profilefacebook;
    private String profiletwitter;
    private String profilelinkedin;
    private String profilegithub;
    private String profilexing;
    private String source;
    
    
    public Candidate(boolean id, String slug, String firstname, String lastname, String emailid, int genderid,
			String contactnumber, String address, String city, String summary, String locality,
			String profilefacebook, String profiletwitter, String profilelinkedin, String profilegithub,
			String profilexing) {
		super();
		this.id = id;
		this.slug = slug;
		this.firstname = firstname;
		this.lastname = lastname;
		this.emailid = emailid;
		this.genderid = genderid;
		this.contactnumber = contactnumber;
		this.address = address;
		this.city = city;
		this.summary = summary;
		this.locality = locality;
		this.profilefacebook = profilefacebook;
		this.profiletwitter = profiletwitter;
		this.profilelinkedin = profilelinkedin;
		this.profilegithub = profilegithub;
		this.profilexing = profilexing;
	}
    
	public boolean isId() {
		return id;
	}
	public void setId(boolean id) {
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
	public String getEmailid() {
		return emailid;
	}
	public void setEmailid(String emailid) {
		this.emailid = emailid;
	}
	public int getGenderid() {
		return genderid;
	}
	public void setGenderid(int genderid) {
		this.genderid = genderid;
	}
	public String getContactnumber() {
		return contactnumber;
	}
	public void setContactnumber(String contactnumber) {
		this.contactnumber = contactnumber;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public int getQualificationid() {
		return qualificationid;
	}
	public void setQualificationid(int qualificationid) {
		this.qualificationid = qualificationid;
	}
	public String getSpecialization() {
		return specialization;
	}
	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}
	public int getWorkexpyr() {
		return workexpyr;
	}
	public void setWorkexpyr(int workexpyr) {
		this.workexpyr = workexpyr;
	}
	public int getWorkexpmonth() {
		return workexpmonth;
	}
	public void setWorkexpmonth(int workexpmonth) {
		this.workexpmonth = workexpmonth;
	}
	public int getRelevantexperience() {
		return relevantexperience;
	}
	public void setRelevantexperience(int relevantexperience) {
		this.relevantexperience = relevantexperience;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getAvailablefrom() {
		return availablefrom;
	}
	public void setAvailablefrom(String availablefrom) {
		this.availablefrom = availablefrom;
	}
	public int getCurrentsalary() {
		return currentsalary;
	}
	public void setCurrentsalary(int currentsalary) {
		this.currentsalary = currentsalary;
	}
	public String getLastorganisation() {
		return lastorganisation;
	}
	public void setLastorganisation(String lastorganisation) {
		this.lastorganisation = lastorganisation;
	}
	public String getSkill() {
		return skill;
	}
	public void setSkill(String skill) {
		this.skill = skill;
	}
	public int getWillingtorelocate() {
		return willingtorelocate;
	}
	public void setWillingtorelocate(int willingtorelocate) {
		this.willingtorelocate = willingtorelocate;
	}
	public int getSalaryexpectation() {
		return salaryexpectation;
	}
	public void setSalaryexpectation(int salaryexpectation) {
		this.salaryexpectation = salaryexpectation;
	}
	public int getSalarytype() {
		return salarytype;
	}
	public void setSalarytype(int salarytype) {
		this.salarytype = salarytype;
	}
	public String getCurrentstatus() {
		return currentstatus;
	}
	public void setCurrentstatus(String currentstatus) {
		this.currentstatus = currentstatus;
	}
	public int getNoticeperiod() {
		return noticeperiod;
	}
	public void setNoticeperiod(int noticeperiod) {
		this.noticeperiod = noticeperiod;
	}
	public int getCurrencyid() {
		return currencyid;
	}
	public void setCurrencyid(int currencyid) {
		this.currencyid = currencyid;
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
	public String getProfilegithub() {
		return profilegithub;
	}
	public void setProfilegithub(String profilegithub) {
		this.profilegithub = profilegithub;
	}
	public String getProfilexing() {
		return profilexing;
	}
	public void setProfilexing(String profilexing) {
		this.profilexing = profilexing;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	

}
