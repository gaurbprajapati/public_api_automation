package io.rcrm.api.pojo.nyma;

public class AttachmentPage {

	private String emailSubject;

	private String email;
	private boolean is_send;
	private String resume;
	private String file_name;



	public AttachmentPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public boolean getSendStatus() {
		return is_send;
	}

	public void setSendStatus(boolean is_send) {
		this.is_send = is_send;
	}
	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}
	public String getFileName() {
		return file_name;
	}

	public void setFileName(String file_name) {
		this.file_name = file_name;
	}
}

